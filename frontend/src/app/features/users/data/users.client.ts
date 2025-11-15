/**
 * @file users.client.ts
 *
 * Low-Level HTTP-Client für die `/users` API-Endpunkte.
 * Stellt Methoden zur Verfügung, um Rohdaten vom Backend abzurufen,
 * z.B. eine Liste aller User (`listAll`) oder die detaillierte
 * Ansicht eines Users inklusive Rollen (`getDetails`).
 */
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '@core/http/api.service';
import { HttpContext } from '@angular/common/http';
import { SUPPRESS_403_REDIRECT } from '@core/http/http-context';
import { User } from '@features/users/domain/user.model';
import { UserDetailDto, UserDto } from './user.dto';

@Injectable({ providedIn: 'root' })
export class UsersClient {
  private api = inject(ApiService);

  // Lädt alle User
  listAll(): Observable<UserDto[]> {
    const ctx = new HttpContext().set(SUPPRESS_403_REDIRECT, true);
    return this.api.get<UserDto[]>('/users', undefined, { context: ctx });
  }

  // Einzelne User mit Rollen + Berechtigungen
  getDetails(username: string): Observable<UserDetailDto> {
    const ctx = new HttpContext().set(SUPPRESS_403_REDIRECT, true);
    return this.api.get<UserDetailDto>(`/users/${encodeURIComponent(username)}/details`, undefined, { context: ctx });
  }
}
