import { Component, signal, computed, Signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { combineLatest, debounceTime, switchMap } from 'rxjs';
import { TicketsService } from '../ticket.service';
import { Page } from '../../../shared/models/paging';
import { HydratedTicket } from '../../../shared/models/types';
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
  sort   = signal<'createdOn:desc' | 'createdOn:asc'>('createdOn:desc');

  constructor(private svc: TicketsService) {}

  private query$ = combineLatest([
    toObservable(this.search),
    toObservable(this.page),
    toObservable(this.size),
    toObservable(this.sort),
  ]).pipe(
    debounceTime(250),
    switchMap(([search, page, pageSize, sort]) =>
      this.svc.list({ search, page, pageSize, sort })
    )
  );

  vm: Signal<Page<HydratedTicket>> = toSignal(this.query$, {
    initialValue: { items: [], total: 0, page: 0, pageSize: 20 },
  });

  onSearchInput(event: Event) {
    const input = event.target as HTMLInputElement;
    this.search.set(input.value);
  }
}
