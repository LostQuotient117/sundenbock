import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NavbarService } from '../../../core/navigation/navbar.service';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  templateUrl: './layout.html',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
})
export class AppLayout {
  private nav = inject(NavbarService);
  private auth = inject(AuthService);

  items = this.nav.visible; // Signal<NavItem[]>

  get year() { return new Date().getFullYear(); }

  logout() { this.auth.logout(true); }
}
