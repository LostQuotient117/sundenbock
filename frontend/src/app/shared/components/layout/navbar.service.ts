import { Injectable, computed, signal, inject } from '@angular/core';
import { NavItem } from './nav-item.model';
import { AuthService } from '../../../core/auth/auth.service';

@Injectable({ providedIn: 'root' })
export class NavbarService {
  private auth = inject(AuthService);

  // zentrale Konfiguration
  private all = signal<NavItem[]>([
    { path: '/tickets',  label: 'Tickets',  icon: 'M3 6h18M3 12h18M3 18h18', requiredPermissions: ['TICKET:READ'] },
    { path: '/projects', label: 'Projects', icon: 'M4 6h16v12H4z',           requiredPermissions: ['PROJECT:READ'] },
    { path: '/health',   label: 'Health',   icon: 'M12 2v20M2 12h20',         requiredRoles: ['ADMIN'] },
    // { path: '/settings', label: 'Settings', requiredRoles: ['ADMIN'] },
  ]);

  // optional: Feature-Flags dynamisch an-/abschaltbar
  setHidden(path: string, hidden: boolean) {
    this.all.update(items => items.map(i => i.path === path ? { ...i, hidden } : i));
  }

  // Sichtbare Einträge = Berechtigung + nicht hidden
  visible = computed(() => {
    const roles = this.auth.roles();
    const perms = this.auth.permissions();

    return this.all().filter(item => {
      if (item.hidden) return false;

      if (item.requiredRoles?.length) {
        const ok = item.requiredRoles.some(r => roles.includes(r));
        if (!ok) return false;
      }
      if (item.requiredPermissions?.length) {
        const ok = item.requiredPermissions.some(p => perms.includes(p));
        if (!ok) return false;
      }
      return true;
    });
  });
}