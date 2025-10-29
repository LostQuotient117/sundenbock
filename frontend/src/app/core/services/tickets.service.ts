import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable, of} from 'rxjs';
import { catchError} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class TicketsService {
  
  private readonly base = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  /** Alle Tickets abrufen */
  getTickets$(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/tickets`).pipe(
      catchError((err) => {
        console.error('Fehler beim Laden der Tickets:', err);
        return of([]);
      })
    );
  }
}
