import { Injectable, computed, signal, inject, effect } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { ApiService } from '../http/api.service';
import { TokenService } from './token.service';

import { decodeJwt } from './utils/jwt.util';
import { User } from '../../features/users/domain/user.model';
import { JwtPayload, AuthenticationRequest, AuthenticationResponse, RegistrationRequest } from './models/authentication.models';
@Injectable({ providedIn: 'root' })
export class AuthService {
  // DI via inject() -> steht schon zur Verfügung bei Feld-Init
  private api    = inject(ApiService);
  private tokens = inject(TokenService);
  private router = inject(Router);

  // _token darf jetzt sicher das gespeicherte Token lesen
  private _token = signal<string | null>(this.tokens.getToken());
  private _me = signal<User | null>(null);

  // öffentlich nutzbare Signals
  token     = computed(() => this._token());
  isLoggedIn= computed(() => !!this._token());
  claims    = computed<JwtPayload | null>(() => decodeJwt(this._token()));
  username   = computed(() => this._me()?.username ?? this.claims()?.sub ?? null);
  expiresAt = computed<number | null>(() => this.claims()?.exp ?? null);

  roles       = computed<string[]>(() => this._me()?.roles ?? this.claims()?.roles ?? []);
  permissions = computed<string[]>(() => this._me()?.permissions ?? (this.claims() as any)?.permissions ?? []);
  
  me = this._me.asReadonly();

  login(body: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.api.post<AuthenticationResponse>('/auth/authenticate', body)
      .pipe(tap(res => this.setToken(res.accessToken)));
  }

  register(body: RegistrationRequest): Observable<AuthenticationResponse> {
    return this.api.post<AuthenticationResponse>('/auth/register', body)
      .pipe(tap(res => this.setToken(res.accessToken)));
  }

  logout(redirectToLogin = true): void {
    this.tokens.clear();
    this._token.set(null);
    this._me.set(null);
    if (redirectToLogin) this.router.navigate(['/login']);
  }

  setToken(token: string): void {
    this.tokens.setToken(token);
    this._token.set(token);
    this._me.set(null);
    this.loadMeOnce();
  }

  private _meLoaded = false;
  loadMeOnce(): void {
    if (this._meLoaded || !this._token()) return;
    this._meLoaded = true;

    this.api.get<User>('/auth/me').subscribe({
      next: me => this._me.set(me),
      error: _ => this.logout(true)
    });
  }

  isExpired(graceSeconds = 0): boolean {
    const exp = this.expiresAt();
    if (!exp) return false;
    const nowSec = Math.floor(Date.now() / 1000);
    return nowSec >= (exp - graceSeconds);
  }

  loadUserDetailsById(id: number) {
    return this.api.get(`/users/${id}/details`);
  }

  hasRole = (role: string) => this.roles().includes(role);
  hasPermission = (perm: string) => this.permissions().includes(perm);

  hasAnyRole = (wanted: string[] | string) => {
    const arr = Array.isArray(wanted) ? wanted : [wanted];
    const mine = this.roles();
    return arr.some(r => mine.includes(r));
  };
  hasAllRoles = (wanted: string[] | string) => {
    const arr = Array.isArray(wanted) ? wanted : [wanted];
    const mine = this.roles();
    return arr.every(r => mine.includes(r));
  };
  hasAnyPermission = (wanted: string[] | string) => {
    const arr = Array.isArray(wanted) ? wanted : [wanted];
    const mine = this.permissions();
    return arr.some(p => mine.includes(p));
  };
  hasAllPermissions = (wanted: string[] | string) => {
    const arr = Array.isArray(wanted) ? wanted : [wanted];
    const mine = this.permissions();
    return arr.every(p => mine.includes(p));
  };

  constructor() {
    // Beim App-Start: falls Token vorhanden, „me“ nachladen
    if (this._token()) this.loadMeOnce();

    // Optional: Auto-Logout genau beim Ablauf
    effect(() => {
      const t = this._token();
      if (!t) return;
      if (this.isExpired(0)) this.logout(true);
    });

    window.addEventListener('storage', (e) => {
      if (e.key === this.tokens.getToken()) {
        const next = this.tokens.getToken();
        this._token.set(next);
        this._me.set(null);
        if (next) this.loadMeOnce(); else this.router.navigate(['/login']);
      }
    });
    
  }
}
