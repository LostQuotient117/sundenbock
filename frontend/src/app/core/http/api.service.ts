import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { buildHttpParams } from '../../shared/utils/http-params';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl;

  get<T>(endpoint: string, params?: Record<string, any>): Observable<T> {
    return this.http.get<T>(`${this.base}${endpoint}`, { params: buildHttpParams(params) });
  }
  post<T>(endpoint: string, body: any, params?: Record<string, any>): Observable<T> {
    return this.http.post<T>(`${this.base}${endpoint}`, body, { params: buildHttpParams(params) });
  }
  put<T>(endpoint: string, body: any, params?: Record<string, any>): Observable<T> {
    return this.http.put<T>(`${this.base}${endpoint}`, body, { params: buildHttpParams(params) });
  }
  delete<T>(endpoint: string, params?: Record<string, any>): Observable<T> {
    return this.http.delete<T>(`${this.base}${endpoint}`, { params: buildHttpParams(params) });
  }
}