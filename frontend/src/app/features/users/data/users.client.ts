import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '@core/http/api.service';
import { HttpContext } from '@angular/common/http';
import { SUPPRESS_403_REDIRECT } from '@core/http/http-context';
import { User } from '@features/users/domain/user.model';

@Injectable({ providedIn: 'root' })
export class UsersClient {
  private api = inject(ApiService);

  // Lädt alle User
  listAll(): Observable<User[]> {
    const ctx = new HttpContext().set(SUPPRESS_403_REDIRECT, true);
    return this.api.get<User[]>('/users', undefined, { context: ctx });
    // Erwartet: GET /api/v1/users -> User[]
  }
}
