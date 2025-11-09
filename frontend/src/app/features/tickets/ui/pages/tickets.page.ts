// app/features/tickets/ui/tickets.page.ts
import { Component, inject, signal, Signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { combineLatest, debounceTime, switchMap, map, finalize, tap } from 'rxjs';
import { TicketsService } from '@features/tickets/domain/ticket.service';
import { Page, SortKey, PageQuery } from '@shared/models/paging';
import { Ticket } from '@features/tickets/domain/ticket.model';
import { CreateTicketDto, TicketDto, TicketStatusDto } from '@features/tickets/data/ticket.dto';
import { TicketList } from '../components/ticket-list/ticket-list';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProjectSelectComponent } from '@shared/components/project-select/project-select';

@Component({
  standalone: true,
  selector: 'tickets-page',
  imports: [TicketList, ReactiveFormsModule, ProjectSelectComponent],
  templateUrl: './tickets.page.html',
})
export class TicketsPage {
  private svc = inject(TicketsService);
  private fb = inject(FormBuilder);
  
  // Filters + Pagination
  search = signal('');
  page   = signal(0);
  size   = signal(20);

  // WICHTIG: Sort ist jetzt exakt der erwartete Typ
  sort   = signal<SortKey<TicketDto>>('createdDate:desc' as SortKey<TicketDto>);

  // States für Create-Pop Up
  showCreate  = signal(false);
  creating    = signal(false);
  createError = signal<string | null>(null);

  //Status Auswahl
  ticketStatuses: readonly TicketStatusDto[] =
    ['CREATED','REOPENED','IN_PROGRESS','RESOLVED','REJECTED','CLOSED'] as const;

  // Reactive Form für Create
  createForm = this.fb.nonNullable.group({
    title: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(200)]),
    description: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(2000)]),
    status: this.fb.nonNullable.control<TicketStatusDto>('CREATED', [Validators.required]),
    responsiblePersonUserName: this.fb.nonNullable.control('', [Validators.required]),
    projectId: this.fb.nonNullable.control<number>(0, { validators: [Validators.required, Validators.min(1)] }),
  });

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

  openCreate() {
    this.createError.set(null);
    this.createForm.reset({
      title: '',
      description: '',
      status: 'CREATED',
      responsiblePersonUserName: '',
      projectId: 0,
    });
    this.showCreate.set(true);
  }

  /** Create-Modal schließen */
  closeCreate() {
    if (!this.creating()) this.showCreate.set(false);
  }

  /** Submit Create */
  submitCreate() {
    if (this.createForm.invalid || this.creating()) return;

    this.creating.set(true);
    this.createError.set(null);

    const payload: CreateTicketDto = this.createForm.getRawValue();

    this.svc.create(payload).pipe(
      finalize(() => this.creating.set(false)),
      tap({
        next: () => {
          // Liste aktualisieren: zurück auf Seite 0 + Stream triggern
          this.page.set(0);
          this.search.set(this.search());
          this.closeCreate();
        },
        error: (err) => {
          this.createError.set(err?.error?.message ?? 'Erstellen fehlgeschlagen.');
        }
      })
    ).subscribe();
  }

reloadAfterDelete() {
  // gleiches Muster wie bei Projects: kurz „wackeln“ und zurück auf Seite 0
  this.page.set(this.page() + 1);
  this.page.set(0);
}
}
