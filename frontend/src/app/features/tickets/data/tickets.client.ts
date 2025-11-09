// app/features/tickets/data/tickets.client.ts
import { Injectable, inject } from '@angular/core';
import { ApiService } from '@core/http/api.service';
import { ResourceClient } from '@core/http/resource-client';
import { TicketDto } from './ticket.dto';
import { Observable } from 'rxjs';
import { Page, PageQuery } from '@shared/models/paging';

@Injectable({ providedIn: 'root' })
export class TicketsClient extends ResourceClient<TicketDto> {
  constructor() {
    super(inject(ApiService), '/tickets');
  }

  override list(q?: PageQuery<TicketDto>): Observable<Page<TicketDto>> {
    return super.list(q);
  }
}
