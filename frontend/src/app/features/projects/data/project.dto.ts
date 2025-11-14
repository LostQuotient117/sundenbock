/**
 * @file project.dto.ts
 *
 * Definiert die Data Transfer Object (DTO) Interfaces für Projekte.
 * Dies sind die Rohdatenstrukturen, wie sie von der Backend-API
 * empfangen oder an sie gesendet werden (z.B. Daten als ISO-Strings).
 */
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
//Create-DTO für Projekt
export type CreateProjectDTO = Pick<ProjectDto, 'title' | 'description' | 'abbreviation'>;
