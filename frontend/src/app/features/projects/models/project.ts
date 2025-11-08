import { UserRef } from "@features/users/models/user";

export interface Project {
  id: number;
  title: string;
  description: string;
  abbreviation: string;
  createdDate: Date;        // ISO string from API
  lastModifiedDate: Date;   // ISO string from API
  createdBy: UserRef;
  lastModifiedBy: UserRef;
}