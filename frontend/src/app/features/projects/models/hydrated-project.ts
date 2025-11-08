import { Project } from './project';

export interface HydratedProject extends Omit<Project, 'createdDate' | 'lastModifiedDate'> {
  createdDate: Date;
  lastModifiedDate: Date;
}   