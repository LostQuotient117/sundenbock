/**
 * @file user-select.ts
 *
 * Logik für die `UserSelectComponent`.
 * Eine wiederverwendbare Komponente zur Auswahl eines Benutzers
 * (speziell eines Entwicklers).
 * Sie filtert die Gesamtbenutzerliste (`listAll`) nach dem Suchtext
 * und prüft anschließend via `details`-Endpoint (mit Caching),
 * ob der User die Rolle "DEVELOPER" hat.
 */
import { Component, Input, HostListener, inject, signal, Signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { UsersService, UserVm } from '@features/users/domain/user.service';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { combineLatest, from, Observable, of, timer } from 'rxjs';
import { catchError, debounceTime, filter, map, mergeMap, shareReplay, take, toArray } from 'rxjs/operators';

@Component({
  standalone: true,
  selector: 'user-select',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-select.html',
})
export class UserSelectComponent {
  private svc = inject(UsersService);

  @Input({ required: true }) control!: FormControl<string>;
  @Input() placeholder = 'Entwickler suchen';

  open = signal(false);
  query = signal('');
  activeIndex = signal(-1);
  errorMsg = signal<string | null>(null);
  readonly minChars = 2;
  readonly pageSize = 10;

  //lädt einmalig alle User
  private allUsers$ = this.svc.listAll();

  //Cache username ist Rolle Developer?
  private devFlagCache = new Map<string, boolean>();

  private inflight = new Map<string, Observable<boolean>>();

  private isDeveloper$(username: string): Observable<boolean> {
    // 1) Memory-Cache-Hit
    if (this.devFlagCache.has(username)) {
      return of(this.devFlagCache.get(username)!);
    }
    // 2) Inflight-Hit
    if (this.inflight.has(username)) {
      return this.inflight.get(username)!;
    }
    // 3) neuer Request
    const obs = this.svc.details(username).pipe(
      map(d => {
        const roles = d.roles ?? [];
        const isDev = roles.includes('ROLE_DEVELOPER') || roles.includes('DEVELOPER');
        this.devFlagCache.set(username, isDev);
        return isDev;
      }),
      catchError(() => {
        this.devFlagCache.set(username, false);
        return of(false);
      }),
      shareReplay(1)
    );

    // Inflight vormerken und nach Abschluss wieder entfernen
    this.inflight.set(username, obs);
    obs.subscribe({ complete: () => this.inflight.delete(username) });

    return obs;
  }

  //nur Benutzer angezeigt, deren Rollen ROLE_DEVELOPER enthalten und deren Name oder Username mit dem Suchtext übereinstimmt
  vm: Signal<UserVm[]> = toSignal(
    combineLatest([this.allUsers$, toObservable(this.query).pipe(debounceTime(200)),
    ]).pipe(
      //nur textuelles Matching
      map(([users, q]) => {
      const s = (q || '').trim().toLowerCase();
      if (s.length < this.minChars) return [] as UserVm[];
      return users.filter((u: UserVm) => {
        const full = `${u.firstName ?? ''} ${u.lastName ?? ''} ${u.username}`.toLowerCase();
        return full.includes(s);
      });
    }),
      // Rollenprüfung
      mergeMap((subset: UserVm[]) => {
        if (!subset.length) return of([] as UserVm[]);
        return from(subset).pipe(
          mergeMap(
            (u) => this.isDeveloper$(u.username).pipe(map(isDev => ({ u, isDev }))),
            3
          ),
          //nur Developer durchlassen
          filter(({ isDev }) => isDev),
          map(({ u }) => u),
          take(this.pageSize),
          toArray()
        );
      })
    ),
    { initialValue: [] }
  );

  toggle() { this.open.update(v => !v); if (this.open()) this.activeIndex.set(-1); }
  openPanel() { this.open.set(true); this.errorMsg.set(null); this.activeIndex.set(-1); }
  closePanel() { this.open.set(false); this.activeIndex.set(-1); }

  onInput(e: Event) {
    const el = e.target as HTMLInputElement;
    this.query.set(el.value ?? '');
    if (!this.open()) this.openPanel();
  }

  choose(u: UserVm) {
    const cached = this.devFlagCache.get(u.username);
    if (cached == false) {
      this.errorMsg.set('Nur Nutzer mit ROLE_DEVELOPER können verantwortlich sein.');
      return;
    }
    this.control.setValue(u.username, { emitEvent: true });

    //zeigt Vor- + Nachname (oder username) im Inputfeld
    const label = `${u.firstName ?? ''} ${u.lastName ?? ''}`.trim() || u.username;
    this.query.set(label);
    this.closePanel();
  }

  // Tastaturnavigation angelehnt an project-select umsetzung
  onKeydown(e: KeyboardEvent) {
    const items = this.vm();
    if (!this.open() && (e.key === 'ArrowDown' || e.key === 'ArrowUp')) { this.openPanel(); e.preventDefault(); return; }
    if (!this.open()) return;

    if (e.key === 'ArrowDown') { this.activeIndex.update(i => Math.min(items.length - 1, i + 1)); e.preventDefault(); }
    else if (e.key === 'ArrowUp') { this.activeIndex.update(i => Math.max(0, i - 1)); e.preventDefault(); }
    else if (e.key === 'Enter') {
      const i = this.activeIndex();
      if (i >= 0 && i < items.length) this.choose(items[i]);
      e.preventDefault();
    } else if (e.key === 'Escape') { this.closePanel(); e.preventDefault(); }
  }

  @HostListener('document:click', ['$event'])
  onDocClick(ev: MouseEvent) {
    const target = ev.target as HTMLElement | null;
    if (!target?.closest('[data-role="user-select-root"]')) this.closePanel();
  }
}

