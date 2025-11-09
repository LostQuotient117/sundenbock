// user.service.ts – mergen (GET -> merge -> PUT), Cache optional
import { Injectable, inject, signal } from '@angular/core';
import { switchMap, tap, map } from 'rxjs';
import { ApiService } from '../http/api.service';
import { AuthService } from '../auth/auth.service';
import { UserClient } from './user.client';
import { User } from '../../features/users/domain/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private api  = inject(ApiService);
  private auth = inject(AuthService);
  private users = new UserClient(this.api);

  private _profile = signal<User | null>(null);
  profile = this._profile.asReadonly();

  /** Eigene Details laden (GET /auth/me liefert UserDetailDTO) */
  loadSelf() {
    return this.api.get<User>('auth/me')
      .pipe(tap(me => this._profile.set(me)));
  }

  updateSelf(patch: Partial<User>) {
    const username = this.auth.username();
    if (!username) throw new Error('Not authenticated');

    return this.users.getDetails(username).pipe(
        map(current => {
        const full: User = {
            ...current,
            ...patch,
            email: patch.email ?? current.email,
            roles: patch.roles ?? current.roles ?? [],
            permissions: patch.permissions ?? current.permissions ?? []
        };
        return full;
        }),
        switchMap(full => this.users.updateByUsername(username, full)),
        tap(updated => this._profile.set(updated))
    );
  }
}
