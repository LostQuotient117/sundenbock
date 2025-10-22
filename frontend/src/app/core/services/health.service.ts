// src/app/core/services/health.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable, of, timer } from 'rxjs';
import { catchError, map, retry, timeout } from 'rxjs/operators';

export type HealthStatus = 'UP' | 'DOWN';
export type HealthResponse = { status: string };

@Injectable({ providedIn: 'root' })
export class HealthService {
  private readonly base = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  /** Einfache Health-Abfrage (einmalig) */
  ping$(): Observable<HealthStatus> {
    return this.http.get<HealthResponse>(`${this.base}/health`).pipe(
      timeout(2000),
      map(res =>
        res?.status?.toUpperCase() === 'UP' ? ('UP' as const) : ('DOWN' as const)
      ),
      catchError(() => of<'DOWN'>('DOWN'))
    );
  }

  /** Mit Retry (3 Versuche, exponentieller Backoff) */
  pingWithRetry$(): Observable<HealthStatus> {
    return this.http.get<HealthResponse>(`${this.base}/health`).pipe(
      timeout(2000),
      map(res =>
        res?.status?.toUpperCase() === 'UP' ? ('UP' as const) : ('DOWN' as const)
      ),
      retry({
        count: 3,
        delay: (_err, i) => timer(300 * Math.pow(2, i)) // 300 ms, 600 ms, 1200 ms
      }),
      catchError(() => of<'DOWN'>('DOWN'))
    );
  }
}
