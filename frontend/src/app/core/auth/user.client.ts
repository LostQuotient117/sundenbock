import { Observable } from 'rxjs';
import { ApiService } from '../http/api.service';
import { ResourceClient } from '../http/resource-client';
import { User } from '../../features/users/models/user';

export class UserClient extends ResourceClient<User> {
  constructor(api: ApiService) { super(api, '/api/v1/users'); }

  /** GET /api/v1/users/{username}/details */
  getDetails(username: string): Observable<User> {
    return this.api.get<User>(`${this.basePath}/${encodeURIComponent(username)}/details`);
  }

  /** PUT /api/v1/users/{username}/update  (Server erwartet komplettes DTO) */
  updateByUsername(username: string, full: User): Observable<User> {
    return this.api.put<User>(`${this.basePath}/${encodeURIComponent(username)}/update`, full);
  }

  /** Rollen & Permissions laut Spec */
  assignRole(username: string, roleName: string) {
    return this.api.put<void>(`${this.basePath}/${encodeURIComponent(username)}/roles/${encodeURIComponent(roleName)}`, {});
  }
  removeRole(username: string, roleName: string) {
    return this.api.delete<void>(`${this.basePath}/${encodeURIComponent(username)}/roles/${encodeURIComponent(roleName)}`);
  }
  assignPermission(username: string, perm: string) {
    return this.api.put<void>(`${this.basePath}/${encodeURIComponent(username)}/permissions/${encodeURIComponent(perm)}`, {});
  }
  removePermission(username: string, perm: string) {
    return this.api.delete<void>(`${this.basePath}/${encodeURIComponent(username)}/permissions/${encodeURIComponent(perm)}`);
  }
}
