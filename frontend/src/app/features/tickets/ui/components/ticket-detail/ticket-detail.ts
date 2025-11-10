// app/features/tickets/ui/components/ticket-detail/ticket-detail.ts
import { Component, computed, effect, inject, signal, Signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { switchMap } from 'rxjs/operators';
import { TicketsService } from '@features/tickets/domain/ticket.service';
import { Ticket, TicketStatus } from '@features/tickets/domain/ticket.model';
import { TicketStatusLabelPipe } from '@shared/pipes/status-label.pipe';
import { CommentsService } from '@features/tickets/domain/comments.service';
import { Page } from '@shared/models/paging';
import { TicketComment } from '@features/tickets/domain/comment.model';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UpdateTicketDto } from '@features/tickets/data/ticket.dto';

@Component({
  standalone: true,
  selector: 'ticket-detail',
  imports: [CommonModule, RouterLink, DatePipe, TicketStatusLabelPipe, ReactiveFormsModule],
  templateUrl: './ticket-detail.html',
})
export class TicketDetail {
  private route = inject(ActivatedRoute);
  private svc = inject(TicketsService);
  private readonly fb = inject(FormBuilder);

  readonly Math = Math;

  private commentsSvc = inject(CommentsService);
  comments = signal<Page<TicketComment> | null>(null);

  constructor() {
    // wenn ticket() einen Wert hat, Kommentare laden
    effect(() => {
      const t = this.ticket();
      if (t?.id) {
        this.commentsSvc
          .listByTicket(t.id, { page: 0, pageSize: 20, sort: 'createdDate:asc' })
          .subscribe((p) => this.comments.set(p));
      }
    });
  }

  // load ticket by :id param
  ticket: Signal<Ticket | undefined> = toSignal(
    this.route.paramMap.pipe(
      switchMap((pm) => this.svc.get(pm.get('id')!)
    )),
    { initialValue: undefined }
  );

  // Styling map for badges (DaisyUI)
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

startEdit() {
  const t = this.ticket();
  if (!t) return;
  this.form.setValue({
    title: t.title,
    description: t.description ?? '',
    status: t.status as TicketStatus,
    projectId: t.project?.id ?? null,
    responsiblePersonUserName: t.responsiblePerson?.username ?? t.responsiblePersonUserName ?? '',
  });
  this.form.markAsPristine();
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

  const base: UpdateTicketDto = {
    id: t.id,
    title: this.form.value.title!,
    description: this.form.value.description!,
    status: this.form.value.status! as any,
    project: { id: this.form.value.projectId! },
    responsiblePerson: { username: this.form.value.responsiblePersonUserName! },
  };

  const body: UpdateTicketDto = t.ticketKey
    ? { ...base, ticketKey: t.ticketKey } // nur wenn gesetzt
    : base;

  this.svc.update(t.id, body).subscribe({
    next: (updated: Ticket) => {
      this.svc.setCurrentTicket(updated);
      this.saving.set(false);
      this.editing.set(false);
    },
    error: (err: any) => {
      this.saving.set(false);
      const msg = (err?.error?.message as string) || 'Speichern fehlgeschlagen.';
      this.saveError.set(msg);
    },
  });
}
}
