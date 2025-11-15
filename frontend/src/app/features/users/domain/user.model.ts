/**
 * @file user.model.ts
 *
 * Definiert die Kern-Domain-Models für `User` und `UserRef`.
 * Diese Interfaces repräsentieren die "saubere" Datenstruktur,
 * die innerhalb der Anwendung verwendet wird (z.B. in Komponenten
 * oder anderen Services).
 */
// app/features/users/domain/user.model.ts
export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  roles: string[];
  permissions: string[];
  createdAt?: string;
  updatedAt?: string;
}

export interface UserRef {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
}