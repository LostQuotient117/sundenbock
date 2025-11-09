export interface UserRefDto {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
}

export interface ProjectDto {
  id: number;
  title: string;
  description: string;
  abbreviation: string;

  // Zeitstempel als string (ISO) – exakt wie vom Backend
  createdDate: string;
  lastModifiedDate: string;

  // optionale Nested-Objekte (laut Backend vorhanden)
  createdBy: UserRefDto;
  lastModifiedBy: UserRefDto;
}
