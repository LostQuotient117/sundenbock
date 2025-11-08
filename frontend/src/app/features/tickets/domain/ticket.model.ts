export const TicketStatus = {
  CREATED: 'CREATED',
  REOPENED: 'REOPENED',
  IN_PROGRESS: 'IN_PROGRESS',
  RESOLVED: 'RESOLVED',
  REJECTED: 'REJECTED',
  CLOSED: 'CLOSED',
} as const;

export type TicketStatus = (typeof TicketStatus)[keyof typeof TicketStatus];

export interface Ticket {
  id: string;
  title: string;
  status: TicketStatus;
  assigneeId?: string;
  createdAt: Date;
  updatedAt: Date;
}