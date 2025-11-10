import { Injectable, inject } from '@angular/core';
import { shareReplay } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { UsersClient } from '../data/users.client';
import { User } from './user.model';
import { UserDetailDto, UserDto } from '../data/user.dto';

@Injectable({ providedIn: 'root' })
export class UsersService {
  private api = inject(UsersClient);

  listAll(): Observable<UserDto[]> {
    return this.api.listAll().pipe(shareReplay(1));
  }

  details(username: string): Observable<UserDetailDto> {
    return this.api.getDetails(username);
  }
}