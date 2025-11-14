// app/features/tickets/ui/components/ticket-detail/ticket-detail.ts
import { Component, computed, effect, inject, signal, Signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs/operators';
import { TicketsService } from '@features/tickets/domain/ticket.service';
import { Ticket, TicketStatus } from '@features/tickets/domain/ticket.model';
import { TicketStatusLabelPipe } from '@shared/pipes/status-label.pipe';
import { CommentsService } from '@features/tickets/domain/comments.service';
import { Page } from '@shared/models/paging';
import { TicketComment } from '@features/tickets/domain/comment.model';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UpdateTicketDto } from '@features/tickets/data/ticket.dto';
import { UserSelectComponent } from '@shared/components/user-select/user-select/user-select';
import { AuthService } from '@core/auth/auth.service';


@Component({
  standalone: true,
  selector: 'ticket-detail',
  imports: [
    CommonModule,
    RouterLink,
    DatePipe,
    TicketStatusLabelPipe,
    ReactiveFormsModule,
    UserSelectComponent,
  ],
  templateUrl: './ticket-detail.html',
})
export class TicketDetail {
  private route = inject(ActivatedRoute);
  private svc = inject(TicketsService);
  private commentsSvc = inject(CommentsService);
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);


  // Für {{ Math.ceil(...) }} im Template
  readonly Math = Math;

  /**
   * Lokale Quelle der Wahrheit fürs Ticket.
   * Alles (Anzeige + Formular + Badge) leitet sich von diesem Signal ab.
   */
  private ticketState = signal<Ticket | undefined>(undefined);

  /** Readonly-Sicht fürs Template: ticket() */
  ticket: Signal<Ticket | undefined> = this.ticketState.asReadonly();

  /** Kommentare zum Ticket */
  comments = signal<Page<TicketComment> | null>(null);

  /** Styling für Status-Badge (DaisyUI) */
  badgeClass = computed(() => {
    const t = this.ticket();
    if (!t) return 'badge';
    switch (t.status) {
      case TicketStatus.CREATED:
      case TicketStatus.REOPENED:
        return 'badge-info';
      case TicketStatus.IN_PROGRESS:
        return 'badge-warning';
      case TicketStatus.RESOLVED:
        return 'badge-success';
      case TicketStatus.REJECTED:
        return 'badge-error';
      case TicketStatus.CLOSED:
      default:
        return 'badge-neutral';
    }
  });

  statusOptions: TicketStatus[] = Object.values(TicketStatus);

  // Helper für Rollen/Benutzer
  private currentUsername(): string | null {
    return this.auth.username();
  }

  private isAdmin(): boolean {
    return this.auth.hasAnyRole(['ADMIN', 'ROLE_ADMIN']);
  }

  private isAuthor(t: Ticket, me: string | null): boolean {
    return !!me && t.createdBy?.username === me;
  }

  private isResponsible(t: Ticket, me: string | null): boolean {
    return !!me && (
      t.responsiblePersonUserName === me ||
      t.responsiblePerson?.username === me
    );
  }

  editing = signal(false);
  saving = signal(false);
  saveError = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
    description: ['', [Validators.required, Validators.maxLength(2000)]],
    status: this.fb.nonNullable.control<TicketStatus>(TicketStatus.CREATED, [Validators.required]),
    projectId: this.fb.nonNullable.control<number | null>(null, [Validators.required]),
    responsiblePersonUserName: this.fb.nonNullable.control<string>('', [Validators.required]),
  });

  constructor() {
    // sicherstellen, dass der eingeloggte User einmal geladen wird (für Assign me / Rollen)
    this.auth.loadMeOnce();
    // Ticket aus :id-Route laden und in ticketState schreiben
    this.route.paramMap
      .pipe(
        switchMap((pm) => {
          const id = pm.get('id');
          if (!id) {
            throw new Error('Ticket-ID fehlt in der URL');
          }
          return this.svc.get(id);
        })
      )
      .subscribe({
        next: (t) => {
          // zentrale Quelle der Wahrheit
          this.ticketState.set(t);

          // Nur wenn wir NICHT im Edit-Modus sind, Formular synchronisieren
          if (!this.editing()) {
            this.patchFormFromTicket(t);
          }
        },
        error: (err) => {
          console.error('Ticket konnte nicht geladen werden', err);
        },
      });

    // Kommentare laden, sobald ein Ticket da ist
    effect(() => {
      const t = this.ticket();
      if (!t?.id) return;

      this.comments.set(null);
      this.commentsSvc
        .listByTicket(t.id, { page: 0, pageSize: 20, sort: 'createdDate:asc' })
        .subscribe({
          next: (p) => this.comments.set(p),
          error: (err) => {
            console.error('Kommentare konnten nicht geladen werden', err);
            // Fallback: leere Seite, damit Template nicht crasht
            this.comments.set({
              items: [],
              total: 0,
              page: 0,
              pageSize: 20,
            });
          },
        });
    });
  }

  /** Ticket -> Formular transferieren */
  private patchFormFromTicket(t: Ticket) {
    this.form.setValue({
      title: t.title,
      description: t.description ?? '',
      status: t.status as TicketStatus,
      projectId: t.project?.id ?? null,
      responsiblePersonUserName: t.responsiblePerson?.username ?? t.responsiblePersonUserName ?? '',
    });
    this.form.markAsPristine();
  }

  getStatusOptionsForEdit(): TicketStatus[] {
    const t = this.ticket();
    if (!t) return this.statusOptions;

    // Admin alle Status
    if (this.isAdmin()) {
      return this.statusOptions;
    }

    const me = this.currentUsername();
    if (!me) {
      return [t.status];
    }

    const isAuthor = this.isAuthor(t, me);
    const isResponsible = this.isResponsible(t, me);

    // Responsible Developer bei in progress
    if (t.status === TicketStatus.IN_PROGRESS && isResponsible) {
      return [
        TicketStatus.IN_PROGRESS,
        TicketStatus.RESOLVED,
        TicketStatus.REJECTED,
      ];
    }

    // Autor bei resolved/rejected
    if (
      isAuthor &&
      (t.status === TicketStatus.RESOLVED || t.status === TicketStatus.REJECTED)
    ) {
      return [
        t.status,
        TicketStatus.CLOSED,
        TicketStatus.REOPENED,
      ];
    }

    // Standard: nur aktueller Status
    return [t.status];
  }

   canEditTicket(): boolean {
    const t = this.ticket();
    if (!t) return false;

    const me = this.currentUsername();
    if (!me) return false;

    // Admin darf immer bearbeiten
    if (this.isAdmin()) {
      return true;
    }
    
    // closed tickets für alle anderen nicht bearbeitbar
    if (t.status === TicketStatus.CLOSED) {
    return false;
  }

  const isAuthor = this.isAuthor(t, me);
  const isResponsible = this.isResponsible(t, me);

  // in prgress und ich bin nicht responsible -> autor darf nicht bearbeiten
  if (
   t.status === TicketStatus.IN_PROGRESS) {
   if (isResponsible) {
        return true;
      }
      if (isAuthor && !isResponsible) {
        return false;
      }
    }
   // Autor darf bearbeiten um auf Closed/reopened zu stellen
    if (
      (t.status === TicketStatus.RESOLVED || t.status === TicketStatus.REJECTED) &&
      isAuthor
    ) {
      return true;
    }

    return isAuthor;
  }

  startEdit() {
    const t = this.ticket();
    if (!t) return;

    // Beim Start des Editierens: aktuelles Ticket ins Formular pumpen
    this.patchFormFromTicket(t);
    this.saveError.set(null);
    this.editing.set(true);
  }

  cancelEdit() {
    this.editing.set(false);
    this.saveError.set(null);
  }

  submitUpdate() {
    const t = this.ticket();
    if (!t || this.form.invalid) return;

    this.saving.set(true);
    this.saveError.set(null);

    const body: UpdateTicketDto = {
      id: t.id,
      title: this.form.value.title!,
      description: this.form.value.description!,
      status: this.form.value.status! as any,
      project: { id: this.form.value.projectId! },
      responsiblePerson: { username: this.form.value.responsiblePersonUserName! },
    };

    this.svc.update(t.id, body).subscribe({
      next: (updated: Ticket) => {
        // Lokale Quelle der Wahrheit aktualisieren
        this.ticketState.set(updated);
        // optional auch im Service weiterreichen (falls anderswo verwendet)
        this.svc.setCurrentTicket(updated);

        // Formular an neuen Stand anpassen
        this.patchFormFromTicket(updated);

        this.saving.set(false);
        this.editing.set(false);
      },
      error: (err: any) => {
        this.saving.set(false);
        const msg = err?.error?.message ?? 'Statuswechsel ist für deine Rolle in diesem Zustand nicht erlaubt.';
        this.saveError.set(msg);
      },
    });
  }

  canSearchDevelopers(): boolean {
    // Admin darf Suche nutzen
    return this.auth.hasAnyRole(['ADMIN', 'ROLE_ADMIN']);
  }

  // Rollenprüfung auf Developer 
  isDeveloper(): boolean {
    return (
      this.auth.hasAnyRole(['DEVELOPER', 'ROLE_DEVELOPER']) &&
      !this.canSearchDevelopers()
    );
  }

  canSelfAssign(): boolean {
    const t = this.ticket()
    if(!t) return false;
    // Developer darf sich selbst zuweisen (wenn nicht Admin)
    if(!this.isDeveloper()) return false;

    return(
      t.status === TicketStatus.CREATED ||
      t.status === TicketStatus.REOPENED
    );
  }

  onDeveloperChosen(u: { username: string }) {
    this.form.patchValue({ responsiblePersonUserName: u.username });
  }

  onAssignMe() {
    const me = this.auth.username();
    const t = this.ticket();
    if (!me || !t) return;
    this.form.patchValue({ responsiblePersonUserName: me });

    if (
      t.status === TicketStatus.CREATED ||
      t.status === TicketStatus.REOPENED
    ) {
      this.form.patchValue({ status: TicketStatus.IN_PROGRESS });
    }
  }

  onAssignMeAndSave() {
    this.onAssignMe();
    this.submitUpdate();
  }
}
