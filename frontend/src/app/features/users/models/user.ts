export interface User {
  id: number;
  username: string;
  email: string;
  enabled: boolean;
  roles: string[];
}