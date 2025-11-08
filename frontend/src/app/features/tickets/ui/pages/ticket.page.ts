// app/features/tickets/ui/tickets.page.ts
import { Component, signal, Signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { combineLatest, debounceTime, switchMap, map } from 'rxjs';

import { TicketsService } from '@features/tickets/domain/ticket.service';
import { Page, SortKey, PageQuery } from '@shared/models/paging';
import { Ticket } from '@features/tickets/domain/ticket.model';
import { TicketDto } from '@features/tickets/data/ticket.dto';
import { TicketList } from '../components/ticket-list/ticket-list';

@Component({
  standalone: true,
  selector: 'tickets-page',
  imports: [TicketList],
  templateUrl: './tickets.page.html',
})
export class TicketsPage {
  // Filters + Pagination
  search = signal('');
  page   = signal(0);
  size   = signal(20);

  // WICHTIG: Sort ist jetzt exakt der erwartete Typ
  sort   = signal<SortKey<TicketDto>>('createdAt:desc' as SortKey<TicketDto>);

  constructor(private svc: TicketsService) {}

  private query$ = combineLatest([
    toObservable(this.search),
    toObservable(this.page),
    toObservable(this.size),
    toObservable(this.sort),
  ]).pipe(
    debounceTime(250),
    map(([search, page, pageSize, sort]) => {
      const q: PageQuery<TicketDto> = { search, page, pageSize, sort };
      return q;
    }),
    switchMap(q => this.svc.list(q))
  );

  vm: Signal<Page<Ticket>> = toSignal(this.query$, {
    initialValue: { items: [], total: 0, page: 0, pageSize: 20 },
  });

  onSearchInput(event: Event) {
    const input = event.target as HTMLInputElement;
    this.search.set(input.value);
  }
}
