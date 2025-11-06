import { ApplicationConfig, importProvidersFrom, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { httpErrorInterceptor } from './core/http/http-error.interceptor';
import { authInterceptor } from './core/auth/auth.inteceptor';
import { LucideAngularModule, Save, Trash2 } from 'lucide-angular';
//import { provideTransloco } from './core/i18n/transloco.config';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor, httpErrorInterceptor])),
    //provideTransloco(),
    importProvidersFrom(
      LucideAngularModule.pick({  Save, Trash2 })
    ),
  ]
};
