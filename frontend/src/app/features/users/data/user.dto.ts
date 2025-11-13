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