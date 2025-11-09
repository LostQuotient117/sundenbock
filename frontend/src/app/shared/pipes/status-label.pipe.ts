import { Pipe, PipeTransform } from '@angular/core';
import { TicketStatus } from '@features/tickets/domain/ticket.model';

const LABELS: Record<TicketStatus, string> = {
  CREATED: 'Created',
  REOPENED: 'Reopened',
  IN_PROGRESS: 'In Progress',
  RESOLVED: 'Resolved',
  REJECTED: 'Rejected',
  CLOSED: 'Closed',
};

@Pipe({ name: 'ticketStatusLabel', standalone: true })
export class TicketStatusLabelPipe implements PipeTransform {
  transform(value: TicketStatus): string { return LABELS[value]; }
}
