/**
 * @file tickets.client.ts
 *
 * Low-Level HTTP-Client für die `/tickets` API-Endpunkte.
 * Erweitert den `ResourceClient` für Standard-CRUD und fügt
 * spezifische Methoden für abweichende Endpunkte hinzu
 * (z.B. `createTicket`, `deleteTicket`, `updateCustom`).
 * Kommuniziert ausschließlich mit rohen DTOs.
 */
// app/features/tickets/data/tickets.client.ts
import { Injectable, inject } from '@angular/core';
import { ApiService } from '@core/http/api.service';
import { ResourceClient } from '@core/http/resource-client';
import { CreateTicketDto, TicketDto, UpdateTicketDto } from './ticket.dto';
import { Observable } from 'rxjs';
import { Page, PageQuery } from '@shared/models/paging';
import { HttpContext } from '@angular/common/http';
import { SUPPRESS_403_REDIRECT } from '@core/http/http-context';

@Injectable({ providedIn: 'root' })
export class TicketsClient extends ResourceClient<TicketDto> {
  constructor() {
    super(inject(ApiService), '/tickets');
  }

  override list(q?: PageQuery<TicketDto>): Observable<Page<TicketDto>> {
    return super.list(q);
  }

  createTicket(body: CreateTicketDto): Observable<TicketDto> {
    const ctx = new HttpContext().set(SUPPRESS_403_REDIRECT, true);
    return this.api.post<TicketDto>('/tickets/create', body, undefined, { context: ctx });
  }

  deleteTicket(id: number | string) {
  const ctx = new HttpContext().set(SUPPRESS_403_REDIRECT, true);
  // Backend Endpunkt: DELETE /tickets/{id}/delete
  return this.api.delete<void>(`/tickets/${id}/delete`, undefined, { context: ctx });
}

updateCustom(id: number, dto: UpdateTicketDto): Observable<TicketDto> {
    return this.api.put<TicketDto>(`/tickets/${id}/update`, dto);
  }
}
