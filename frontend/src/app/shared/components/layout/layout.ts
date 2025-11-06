import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NavbarService } from '../../../core/navigation/navbar.service';
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
