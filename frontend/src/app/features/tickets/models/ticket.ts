export interface Ticket {
  id: number;
  title: string;
  description?: string;
  status: 'CREATED' | 'REOPENED' | 'IN_PROGRESS' | 'RESOLVED' | 'REJECTED' | 'CLOSED';
  author: User;
  responsiblePerson?: User;
  project?: Project;
  createdOn: Date;
  lastChange: Date;
}