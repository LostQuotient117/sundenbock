import { Injectable, inject } from '@angular/core';
import { map, shareReplay } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { UsersClient } from '../data/users.client';
import { User } from './user.model';
import { UserDetailDto, UserDto } from '../data/user.dto';

export type UserVm = Omit<UserDto, 'createdAt' | 'updatedAt'> & {
  createdAt: Date;
  updatedAt: Date;
};

@Injectable({ providedIn: 'root' })
export class UsersService {
  private api = inject(UsersClient);

  listAll(): Observable<UserVm[]> {
    return this.api.listAll().pipe(
      map(dtos =>
        dtos.map(d => ({
          ...d,
          createdAt: new Date(d.createdAt),
          updatedAt: new Date(d.updatedAt),
        }))
      ),
      shareReplay(1)
    );
  }

  details(username: string): Observable<UserDetailDto> {
    return this.api.getDetails(username);
  }
}