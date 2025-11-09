import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-auth-shell',
  imports: [RouterOutlet],
  templateUrl: './auth-shell.html',
})
export class AuthShell {
  currentYear = new Date().getFullYear();
}
