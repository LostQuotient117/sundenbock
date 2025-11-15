/**
 * @file user.dto.ts
 *
 * Definiert die Data Transfer Object (DTO) Interfaces für Benutzer.
 * `UserDto` ist die Basisstruktur von der API.
 * `UserDetailDto` ist eine erweiterte Struktur, die zusätzlich
 * Rollen und Berechtigungen für einen Benutzer enthält.
 */
export interface UserDto {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * Erweiterte Detailstruktur für GET /api/v1/users/{username}/details
 * enthält Rollen und Berechtigungen
 */
export interface UserDetailDto extends UserDto {
  roles: string[];
  permissions: string[];
}