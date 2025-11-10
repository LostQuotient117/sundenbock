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
import { FormBuilder, Validators } from '@angular/forms';

@Component({
  standalone: true,
  selector: 'ticket-detail',
  imports: [CommonModule, RouterLink, DatePipe, TicketStatusLabelPipe],
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

editing = signal(false);
saving = signal(false);
saveError = signal<string | null>(null);

form = this.fb.nonNullable.group({
  title: ['', [Validators.required, Validators.maxLength(200)]],
  description: ['', [Validators.required, Validators.maxLength(2000)]],
  status: this.fb.nonNullable.control<string>('CREATED', [Validators.required]),
  projectId: this.fb.nonNullable.control<number | null>(null, [Validators.required]),
  responsiblePersonUserName: this.fb.nonNullable.control<string>('', [Validators.required]),
});

startEdit() {
  const t = this.ticket();
  if (!t) return;
  this.form.setValue({
    title: t.title,
    description: t.description ?? '',
    status: (t.status as string) ?? 'CREATED',
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
}
