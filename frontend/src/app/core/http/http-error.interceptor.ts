import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { TokenService } from '../auth/token.service';
import { NotificationService } from '../../shared/components/notification/notification.service';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const tokenSvc = inject(TokenService);
  const notify = inject(NotificationService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // ===================== 1. Kein Netz =====================
      if (error.status === 0) {
        console.error('❌ Keine Verbindung zum Server.');
        notify.show('Keine Verbindung zum Server.');
      }

      // ===================== 2. Unauthorized ==================
      else if (error.status === 401) {
        console.warn('⚠️ Nicht autorisiert.');
        tokenSvc.clear();
        notify.show('Sitzung abgelaufen – bitte erneut anmelden.');
        router.navigate(['/login']);
      }

      // ===================== 3. Forbidden =====================
      else if (error.status === 403) {
        console.warn('🚫 Zugriff verweigert.');
        notify.show('Keine Berechtigung für diese Aktion.');
        router.navigate(['/forbidden']);
      }

      // ===================== 4. Not Found =====================
      else if (error.status === 404) {
        console.warn('❓ Ressource nicht gefunden:', req.url);
        notify.show('Ressource wurde nicht gefunden.');
      }

      // ===================== 5. Server Error ==================
      else if (error.status >= 500) {
        console.error('💥 Serverfehler:', error.message);
        notify.show('Serverfehler – bitte später erneut versuchen.');
      }

      // ===================== 6. Fehler weiterreichen ==========
      return throwError(() => error);
    })
  );
};