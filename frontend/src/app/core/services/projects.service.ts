import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { catchError, Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProjectsService {
  
  private readonly base = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getProjects$(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/projects`).pipe(
      catchError((err) => {
        console.error('Fehler beim Laden der Projekte:', err);
        return of([]);
      })
    );
  }
}
