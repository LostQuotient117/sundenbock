import { Injectable, computed, signal, inject } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../http/api.service';
import { TokenService } from './token.service';
import { Observable, tap } from 'rxjs';

export interface AuthenticationRequest { username: string; password: string; }
export interface RegistrationRequest { username: string; email: string; password: string; }
export interface AuthenticationResponse { token: string; }

interface JwtPayload {
  sub?: string;
  exp?: number;
  roles?: string[];
  [k: string]: any;
}

function decodeJwt(token: string | null): JwtPayload | null {
  if (!token) return null;
  const parts = token.split('.');
  if (parts.length !== 3) return null;
  try {
    // base64url -> base64
    const json = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(json);
  } catch {
    return null;
  }
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  // DI via inject() -> steht schon zur Verfügung bei Feld-Init
  private api    = inject(ApiService);
  private tokens = inject(TokenService);
  private router = inject(Router);

  // _token darf jetzt sicher das gespeicherte Token lesen
  private _token = signal<string | null>(this.tokens.get());

  // öffentlich nutzbare Signals
  token     = computed(() => this._token());
  isLoggedIn= computed(() => !!this._token());
  claims    = computed<JwtPayload | null>(() => decodeJwt(this._token()));
  username  = computed(() => this.claims()?.sub ?? null);
  expiresAt = computed<number | null>(() => this.claims()?.exp ?? null);

  login(body: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.api.post<AuthenticationResponse>('/auth/authenticate', body)
      .pipe(tap(res => this.setToken(res.token)));
  }

  register(body: RegistrationRequest): Observable<AuthenticationResponse> {
    return this.api.post<AuthenticationResponse>('/auth/register', body)
      .pipe(tap(res => this.setToken(res.token)));
  }

  logout(redirectToLogin = true): void {
    this.tokens.clear();
    this._token.set(null);
    if (redirectToLogin) this.router.navigate(['/login']);
  }

  setToken(token: string): void {
    this.tokens.set(token);
    this._token.set(token);
  }

  isExpired(graceSeconds = 0): boolean {
    const exp = this.expiresAt();
    if (!exp) return false;
    const nowSec = Math.floor(Date.now() / 1000);
    return nowSec >= (exp - graceSeconds);
    // optional: wenn abgelaufen -> this.logout();
  }

  loadUserDetailsById(id: number) {
    // Typ bei Bedarf konkretisieren (UserDetailDTO)
    return this.api.get(`/users/${id}/details`);
  }
}
