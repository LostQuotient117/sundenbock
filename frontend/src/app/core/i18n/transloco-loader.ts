import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TranslocoLoader } from '@jsverse/transloco';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TranslocoHttpLoader implements TranslocoLoader {
  constructor(private http: HttpClient) {}
  getTranslation(lang: string): Observable<any> {
    // lädt aus /i18n/<lang>.json – weil public/ beim Build an den Webroot geht
    return this.http.get(`/i18n/${lang}.json`);
  }
}
