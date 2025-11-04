import { Injectable, computed, signal, inject } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../http/api.service';
import { TokenService } from './token.service';
import { Observable, tap } from 'rxjs';
import { JwtPayload } from './models/jwt-payload.model';
import { AuthenticationResponse } from './models/auth-response.model';
import { AuthenticationRequest, RegistrationRequest } from './models/auth-request.model';
import { decodeJwt } from './utils/jwt.util';
@Injectable({ providedIn: 'root' })
export class AuthService {
  // DI via inject() -> steht schon zur Verfügung bei Feld-Init
  private api    = inject(ApiService);
  private tokens = inject(TokenService);
  private router = inject(Router);

  // _token darf jetzt sicher das gespeicherte Token lesen
  private _token = signal<string | null>(this.tokens.getToken());

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
    this.tokens.setToken(token);
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
