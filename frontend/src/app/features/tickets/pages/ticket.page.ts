import { Component, signal, effect, computed } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { debounceTime, switchMap } from 'rxjs/operators';
import { combineLatest, of } from 'rxjs';
import { TicketsService } from '../ticket.service';

@Component({
  standalone: true,
  selector: 'tickets-page',
  template: `
    <input class="input input-bordered mb-3 w-full sm:w-80" placeholder="Suche…"
      [value]="search()" (input)="search.set(($event.target as HTMLInputElement).value)" />

    <!-- DataTable oder CardGrid hier einbinden -->
    <pre *ngIf="vm()?.items as items">{{ items | json }}</pre>
  `
})
export class TicketsPage {
  search = signal('');
  page   = signal(0);
  size   = signal(20);
  sort   = signal<'createdOn:desc'|'createdOn:asc'>('createdOn:desc');

  private query$ = combineLatest([this.search.toObservable(), this.page.toObservable(), this.size.toObservable(), this.sort.toObservable()])
    .pipe(
      debounceTime(250),
      switchMap(([search, page, pageSize, sort]) =>
        this.svc.list({ search, page, pageSize, sort })
      )
    );

  vm = toSignal(this.query$, { initialValue: { items: [], total: 0, page: 0, pageSize: 20 } });

  constructor(private svc: TicketsService) {}
}
