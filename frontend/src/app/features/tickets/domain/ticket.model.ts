/**
 * @file comment.model.ts
 *
 * Definiert das Kern-Domain-Model `TicketComment` und `CommentAuthor`.
 * Diese Interfaces repräsentieren die "saubere" Datenstruktur,
 * die innerhalb der Angular-Anwendung verwendet wird (z.B. mit
 * `Date`-Objekten statt ISO-Strings).
 */
export const TicketStatus = {
  CREATED: 'CREATED',
  REOPENED: 'REOPENED',
  IN_PROGRESS: 'IN_PROGRESS',
  RESOLVED: 'RESOLVED',
  REJECTED: 'REJECTED',
  CLOSED: 'CLOSED',
} as const;

export type TicketStatus = (typeof TicketStatus)[keyof typeof TicketStatus];

export interface TicketResponsible {
  id: number;
  username: string;
  firstName?: string;
  lastName?: string;
}

export interface TicketProjectRef {
  id: number;
  title?: string;
  abbreviation?: string;
}

export interface Ticket {
  id: string;
  title: string;
  description?: string;
  project?: TicketProjectRef;
  status: TicketStatus;
  ticketKey?: string;
  responsiblePerson?: TicketResponsible;
  responsiblePersonUserName?: string;
  assigneeId?: string;
  createdDate: Date;
  lastModifiedDate: Date;
  createdBy?: TicketResponsible;
  lastModifiedBy?: TicketResponsible;
}