/**
 * @file project.model.ts
 *
 * Definiert das Kern-Domain-Model `Project`.
 * Dieses Model wird innerhalb der Anwendung verwendet (z.B. in Services
 * und Komponenten) und nutzt native Typen wie `Date` anstelle von Strings.
 */
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
