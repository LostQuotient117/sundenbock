import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Ticket, TicketStatus } from '@features/tickets/domain/ticket.model';
import { TicketStatusLabelPipe } from '@shared/pipes/status-label.pipe';

@Component({
  selector: 'app-ticket-list',
  imports: [CommonModule, RouterModule, TicketStatusLabelPipe],
  templateUrl: './ticket-list.html',
  styleUrl: './ticket-list.css'
})
export class TicketList {
  @Input({ required: true }) tickets: Ticket[] = [];

  readonly allStatuses = Object.values(TicketStatus);

  badge: Record<Ticket['status'], string> = {
    [TicketStatus.CREATED]: 'badge-info',
    [TicketStatus.REOPENED]: 'badge-info',
    [TicketStatus.IN_PROGRESS]: 'badge-warning',
    [TicketStatus.RESOLVED]: 'badge-success',
    [TicketStatus.REJECTED]: 'badge-error',
    [TicketStatus.CLOSED]: 'badge-neutral',
  };
}
