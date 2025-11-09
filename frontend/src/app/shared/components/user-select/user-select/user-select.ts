import { Component, Input, HostListener, inject, signal, Signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { UsersService } from '@features/users/domain/user.service';
import { User } from '@features/users/domain/user.model';
import { toSignal } from '@angular/core/rxjs-interop';
import { combineLatest, timer } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  standalone: true,
  selector: 'user-select',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-select.html',
})
export class UserSelectComponent {
  private svc = inject(UsersService);

  @Input({ required: true }) control!: FormControl<string>;
  @Input() placeholder = 'Entwickler auswählen (Vor-/Nachname/Username)…';

  open = signal(false);
  query = signal('');
  activeIndex = signal(-1);
  errorMsg = signal<string | null>(null);
  readonly minChars = 2;
  readonly pageSize = 10;

  //lädt einmalig alle User
  private allUsers$ = this.svc.listAll();

  //nur Benutzer angezeigt, deren Rollen ROLE_DEVELOPER enthalten und deren Name oder Username mit dem Suchtext übereinstimmt
  vm: Signal<User[]> = toSignal(
    combineLatest([this.allUsers$, timer(0, 1).pipe(map(() => this.query()))]).pipe(
      map(([users, q]) => {
        const s = (q || '').trim().toLowerCase();
        if (s.length < this.minChars) return [];
        const isDev = (u: User) => (u.roles ?? []).includes('ROLE_DEVELOPER');
        const full = (u: User) => `${(u as any).firstName ?? ''} ${(u as any).lastName ?? ''} ${u.username}`.toLowerCase();
        return users.filter(u => isDev(u) && full(u).includes(s)).slice(0, this.pageSize);
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

  choose(u: User) {
    if (!(u.roles ?? []).includes('ROLE_DEVELOPER')) {
      this.errorMsg.set('Nur Nutzer mit ROLE_DEVELOPER können verantwortlich sein.');
      return;
    }
    this.control.setValue(u.username, { emitEvent: true });

    //zeigt Vor- + Nachname (oder username) im Inputfeld
    const label = `${(u as any).firstName ?? ''} ${(u as any).lastName ?? ''}`.trim() || u.username;
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

