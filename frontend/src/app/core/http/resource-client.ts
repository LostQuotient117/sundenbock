import { Observable, map } from 'rxjs';
import { ApiService } from './api.service';
import { Page, PageQuery } from '../../shared/models/paging';

export class ResourceClient<T> {
  constructor(protected api: ApiService, protected basePath: string) {}

  list(q?: PageQuery<T>): Observable<Page<T>> {
    return this.api.get<Page<T>>(this.basePath, q);
  }
  get(id: string | number): Observable<T> {
    return this.api.get<T>(`${this.basePath}/${id}`);
  }
  create<B = Partial<T>>(body: B): Observable<T> {
    return this.api.post<T>(this.basePath, body);
  }
  update<B = Partial<T>>(id: string | number, body: B): Observable<T> {
    return this.api.put<T>(`${this.basePath}/${id}`, body);
  }
  delete(id: string | number): Observable<void> {
    return this.api.delete<void>(`${this.basePath}/${id}`);
  }
}
