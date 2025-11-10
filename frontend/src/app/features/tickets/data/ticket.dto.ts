export type TicketStatusDto =
  | 'CREATED'
  | 'REOPENED'
  | 'IN_PROGRESS'
  | 'RESOLVED'
  | 'REJECTED'
  | 'CLOSED';

  export interface TicketResponsiblePersonDto {
  id: number;
  username: string;
  firstName?: string;
  lastName?: string;
}
  
export interface TicketDto {
  id: string;
  title: string;
  status: TicketStatusDto;
  ticketKey?: string;
  responsiblePerson?: TicketResponsiblePersonDto;
  responsiblePersonUserName?: string;
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