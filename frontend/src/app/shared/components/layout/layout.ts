/**
 * @file layout.ts
 *
 * Logik für die `AppLayout`-Komponente (Haupt-Layout der Anwendung).
 * Diese Komponente stellt den Rahmen (inkl. Navbar und Footer) bereit
 * und rendert den Inhalt der aktuellen Route via `<router-outlet>`.
 * Sie holt sich die sichtbaren Navigations-Items vom `NavbarService`
 * und bindet die Logout-Funktion an den `AuthService`.
 */
import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NavbarService } from './navbar.service';
import { AuthService } from '../../../core/auth/auth.service';
import { LucideAngularModule, LogOutIcon } from 'lucide-angular';

@Component({
  selector: 'app-layout',
  standalone: true,
  templateUrl: './layout.html',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LucideAngularModule]
})
export class AppLayout {
  private nav = inject(NavbarService);
  private auth = inject(AuthService);

  items = this.nav.visible;
  readonly LogOutIcon = LogOutIcon;

  get year() { return new Date().getFullYear(); }
  logout() { this.auth.logout(true); }
}
