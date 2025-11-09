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
  createdDate: string;   // ISO String vom Backend
  lastModifiedDate: string;   // ISO String vom Backend
}

//für ticket Create benötigt
export interface CreateTicketDto {
  title: string;
  description: string;
  status: TicketStatusDto;
  responsiblePersonUserName: string;
  projectId: number;
}