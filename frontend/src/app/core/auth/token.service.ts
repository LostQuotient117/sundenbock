import { Injectable } from '@angular/core';
import { decodeJwt } from './utils/jwt.util';
import { JwtPayload } from './models/jwt-payload.model';

type StoredToken = { token: string; exp?: number };
const STORAGE_KEY = 'auth_token';

@Injectable({ providedIn: 'root' })
export class TokenService {

  setToken(token: string, remember = true): void {
    const payload = decodeJwt(token);
    const data: StoredToken = { token, exp: payload?.exp };
    const json = JSON.stringify(data);
    // erst mal überall weg
    localStorage.removeItem(STORAGE_KEY);
    sessionStorage.removeItem(STORAGE_KEY);
    (remember ? localStorage : sessionStorage).setItem(STORAGE_KEY, json);
  }

  getToken(): string | null {
    const data = this.readRaw();
    if (!data) return null;
    if (this.isExpired(data.exp)) {
      this.clear();
      return null;
    }
    return data.token;
  }

  isUnauthenticated(): boolean {
    const data = this.readRaw();
    return !data || this.isExpired(data.exp);
  }

  clear(): void {
    localStorage.removeItem(STORAGE_KEY);
    sessionStorage.removeItem(STORAGE_KEY);
  }

  getPayload(): JwtPayload | null {
    const t = this.getToken();
    return decodeJwt(t);
  }

  // -------- helpers --------
  private readRaw(): StoredToken | null {
    const raw = sessionStorage.getItem(STORAGE_KEY) ?? localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    try { return JSON.parse(raw) as StoredToken; } catch { return null; }
  }

  private isExpired(exp?: number): boolean {
    if (!exp) return false;
    return Math.floor(Date.now() / 1000) >= exp;
  }
}
