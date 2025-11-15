/**
 * @file tickets.service.ts
 *
 * Domain-Service (Fassade) für Ticket-Operationen.
 * Dient als zentrale Schnittstelle für die UI-Schicht, um mit
 * Tickets zu interagieren. Abstrahiert den `TicketsClient`.
 * Verantwortlich für CRUD-Operationen (list, get, create, update, delete)
 * und das Mapping von DTOs zu Domain-Modellen (`mapTicket`).
 */
// app/features/tickets/domain/tickets.service.ts
import { Injectable, inject, signal } from '@angular/core';
import { TicketsClient } from '../data/tickets.client';
import { map, tap } from 'rxjs/operators';
import { Ticket } from './ticket.model';
import { CreateTicketDto, TicketDto, UpdateTicketDto } from '../data/ticket.dto';
import { Page, PageQuery } from '@shared/models/paging';
import { Observable } from 'rxjs';
import { mapTicket } from './ticket.mapper';
import { mapPage } from '@shared/utils/mapping.base'; // <— du HAST diese Funktion (s. unten)

@Injectable({ providedIn: 'root' })
export class TicketsService {
  private api = inject(TicketsClient);

  currentTicket = signal<Ticket | null>(null);

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

update(id: string | number, dto: UpdateTicketDto): Observable<Ticket> {
    return this.api.updateCustom(+id, dto).pipe(map(mapTicket));
  }

  setCurrentTicket(t: Ticket): void {
    this.currentTicket.set(t);
  }
}

