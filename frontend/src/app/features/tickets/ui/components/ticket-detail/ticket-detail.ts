// app/features/tickets/ui/components/ticket-detail/ticket-detail.ts
import { Component, computed, inject, Signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { switchMap } from 'rxjs/operators';
import { TicketsService } from '@features/tickets/domain/ticket.service';
import { Ticket, TicketStatus } from '@features/tickets/domain/ticket.model';
import { TicketStatusLabelPipe } from '@shared/pipes/status-label.pipe';

@Component({
  standalone: true,
  selector: 'ticket-detail',
  imports: [CommonModule, RouterLink, DatePipe, TicketStatusLabelPipe],
  templateUrl: './ticket-detail.html',
})
export class TicketDetail {
  private route = inject(ActivatedRoute);
  private svc = inject(TicketsService);

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
}
