// app/features/tickets/domain/tickets.service.ts
import { Injectable, inject } from '@angular/core';
import { TicketsClient } from '../data/tickets.client';
import { map } from 'rxjs/operators';
import { Ticket } from './ticket.model';
import { TicketDto } from '../data/ticket.dto';
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

  // Falls du create/update brauchst, Beispiele:
  // create(payload: CreateTicketForm): Observable<Ticket> {
  //   const dto: TicketDtoCreate = mapCreateFormToDto(payload);
  //   return this.api.create(dto).pipe(map(mapTicket));
  // }
  //
  // update(id: string, patch: Partial<Ticket>): Observable<Ticket> {
  //   const dto = mapTicketToDto({ ...existing, ...patch });
  //   return this.api.update(id, dto).pipe(map(mapTicket));
  // }
}
