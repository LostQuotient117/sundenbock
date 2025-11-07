import { Injectable, inject } from '@angular/core';
import { ApiService } from '../../core/http/api.service';
import { ResourceClient } from '../../core/http/resource-client';
import { Ticket } from './models/ticket';
import { Page, PageQuery } from '../../shared/models/paging';
import { Observable, map, of } from 'rxjs';
import { HydratedTicket } from '../../shared/models/types';
import { MOCK_TICKETS } from '../tickets/mock-tickets';


@Injectable({ providedIn: 'root' })
export class TicketsService extends ResourceClient<Ticket> {
  constructor() { super(inject(ApiService), '/tickets'); }

  override list(q?: PageQuery<Ticket>): Observable<Page<HydratedTicket>> {
    //return of(MOCK_TICKETS);
    return super.list(q).pipe(
      map(p => ({
        ...p,
        items: p.items.map(t => ({
          ...t,
          createdOn: new Date(t.createdOn),
          lastChange: new Date(t.lastChange),
        }))
      }))
    );
  }
}
