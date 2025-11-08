import { Project } from "../../projects/models/project";
import { User } from "../../users/models/user";

export interface Ticket {
  id: number;
  ticketKey: string;
  title: string;
  description?: string;
  status: 'CREATED' | 'REOPENED' | 'IN_PROGRESS' | 'RESOLVED' | 'REJECTED' | 'CLOSED';
  //author: User;
  responsiblePerson?: User;
  project?: Project;
  comments?: Comment[];
  createdOn: Date;
  lastChange: Date;
}