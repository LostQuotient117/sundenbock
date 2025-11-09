// app/features/tickets/domain/tickets.service.ts
import { Injectable, inject } from '@angular/core';
import { TicketsClient } from '../data/tickets.client';
import { map } from 'rxjs/operators';
import { Ticket } from './ticket.model';
import { CreateTicketDto, TicketDto } from '../data/ticket.dto';
import { Page, PageQuery } from '@shared/models/paging';
import { Observable } from 'rxjs';
import { mapTicket } from './ticket.mapper';
import { mapPage } from '@shared/utils/mapping.base'; // <— du HAST diese Funktion (s. unten)

@Injectable({ providedIn: 'root' })
export class TicketsService {
  private api = inject(TicketsClient);

  list(q?: PageQuery<TicketDto>): Observable<Page<Ticket>> {
    return this.api.list(q).pipe(map(p => mapPage(p, mapTicket)));
  }

  get(id: string): Observable<Ticket> {
    return this.api.get(id).pipe(map(mapTicket));
  }

  // create analog project pattern
  create(dto: CreateTicketDto): Observable<Ticket> {
    return this.api.createTicket(dto).pipe(map(mapTicket));
  }

  delete(id: number | string) {
  return this.api.deleteTicket(id);
}
}
