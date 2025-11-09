import { Injectable, inject } from '@angular/core';
import { shareReplay } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { UsersClient } from '../data/users.client';
import { User } from './user.model';

@Injectable({ providedIn: 'root' })
export class UsersService {
  private api = inject(UsersClient);

  listAll(): Observable<User[]> {
    return this.api.listAll().pipe(shareReplay(1));
  }
}