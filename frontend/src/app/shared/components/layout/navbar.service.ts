/**
 * @file navbar.service.ts
 *
 * Verwaltet den Zustand der Hauptnavigation.
 * Hält die zentrale Konfiguration aller Navigations-Einträge (`all`).
 * Berechnet (`computed`) die aktuell sichtbaren Einträge (`visible`)
 * basierend auf Berechtigungen (zukünftig) und "hidden"-Flags.
 */
import { Injectable, computed, signal, inject } from '@angular/core';
import { NavItem } from './nav-item.model';

@Injectable({ providedIn: 'root' })
export class NavbarService {

  // zentrale Konfiguration
  private all = signal<NavItem[]>([
    { path: '/tickets',  label: 'Tickets',  icon: 'M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9l-7-7z' },
    { path: '/projects', label: 'Projects', icon: 'M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z'},
    // { path: '/settings', label: 'Settings', requiredRoles: ['ADMIN'] },
  ]);

  // Feature-Flags dynamisch an-/abschaltbar
  setHidden(path: string, hidden: boolean) {
    this.all.update(items => items.map(i => i.path === path ? { ...i, hidden } : i));
  }

  // Sichtbare Einträge = Berechtigung + nicht hidden
  visible = computed(() => {
    return this.all().filter(item => !item.hidden);
  });
}