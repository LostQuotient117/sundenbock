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