import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '@core/auth/auth.service';
//import { TranslocoPipe } from '@jsverse/transloco';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  currentYear = new Date().getFullYear();

  submitting = signal(false);
  serverError = signal<string | null>(null);

  form = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
    rememberMe: [true]
  });

  async submit() {
    if (this.form.invalid || this.submitting()) return;
    this.submitting.set(true);
    this.serverError.set(null);

    const { username, password, rememberMe } = this.form.getRawValue();

    try {
      const res = await firstValueFrom(
        this.auth.login({ username: username!, password: password! })
      );

      const token = res.accessToken ?? (res as any).token;
      if (!token) throw new Error('Kein Token erhalten.');

      this.auth.setToken(token); // nutzt jetzt zentral den AuthService

      await this.router.navigateByUrl('/');
    } catch (err: any) {
      console.error('[Login Error]', err);
      this.serverError.set(err?.error?.message || err?.message || 'Login fehlgeschlagen');
    } finally {
      this.submitting.set(false);
    }
  }
}
