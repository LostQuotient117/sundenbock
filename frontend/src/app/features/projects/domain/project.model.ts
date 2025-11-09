export interface UserRef {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
}

export interface Project {
  id: number;
  title: string;
  description: string;
  abbreviation: string;

  // Domain: Dates als Date
  createdDate: Date;
  lastModifiedDate: Date;

  createdBy: UserRef;
  lastModifiedBy: UserRef;
}
