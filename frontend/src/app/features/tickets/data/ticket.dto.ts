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

export interface TicketProjectDto {
  id: number;
  title?: string;
  abbreviation?: string;
}
  
export interface TicketDto {
  id: string;
  title: string;
  description?: string
  status: TicketStatusDto;
  ticketKey?: string;
  responsiblePerson?: TicketResponsiblePersonDto;
  responsiblePersonUserName?: string;
  assigneeId?: string;
  project?: TicketProjectDto;
  createdDate: string;   // ISO String vom Backend
  lastModifiedDate: string;   // ISO String vom Backend
  createdBy?: TicketResponsiblePersonDto
  lastModifiedBy?: TicketResponsiblePersonDto
}

//für ticket Create benötigt
export interface CreateTicketDto {
  title: string;
  description: string;
  status: TicketStatusDto;
  responsiblePersonUserName?: string;
  projectId: number;
}

//für ticket update benötigt
export interface UpdateTicketDto {
  id: string | number;
  title: string;
  description: string;
  status: TicketStatusDto;
  project: { id: number };
  responsiblePerson: { username: string };
  ticketKey?: string; // optional mitsenden, falls vorhanden
}