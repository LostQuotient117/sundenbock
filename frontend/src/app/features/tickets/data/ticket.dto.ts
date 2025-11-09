export type TicketStatusDto =
  | 'CREATED'
  | 'REOPENED'
  | 'IN_PROGRESS'
  | 'RESOLVED'
  | 'REJECTED'
  | 'CLOSED';
  
export interface TicketDto {
  id: string;
  title: string;
  status: TicketStatusDto;
  assigneeId?: string;
  createdAt: string;   // ISO String vom Backend
  updatedAt: string;   // ISO String vom Backend
}