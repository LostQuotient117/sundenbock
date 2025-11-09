import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule, ValidationErrors, AbstractControl } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
//import { TranslocoPipe } from '@jsverse/transloco';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '@core/auth/auth.service';

function passwordsMatch(ctrl: AbstractControl): ValidationErrors | null {
  const pwd = ctrl.get('password')?.value;
  const rep = ctrl.get('confirmPassword')?.value;
  return pwd && rep && pwd !== rep ? { passwordsMismatch: true } : null;
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  submitting = signal(false);
  serverError = signal<string | null>(null);
  currentYear = new Date().getFullYear();

  form = this.fb.group({
    
    firstName: ['', [Validators.required, Validators.minLength(2)]],
    lastName: ['', [Validators.required, Validators.minLength(2)]],
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email]],
    passwordGroup: this.fb.group(
      {
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required]]
      },
      { validators: [passwordsMatch] }
    ),
    roles: this.fb.control<string[]>([])
  });

  
  firstName = this.form.controls.firstName;
  lastName = this.form.controls.lastName;
  username = this.form.controls.username;
  email = this.form.controls.email;
  passwordGroup = this.form.controls.passwordGroup;
  password = this.passwordGroup.get('password')!;
  confirmPassword = this.passwordGroup.get('confirmPassword')!;
  invalidPasswords = computed(() => this.passwordGroup.touched && this.passwordGroup.hasError('passwordsMismatch'));

  async submit() {
    if (this.form.invalid || this.submitting()) return;
    this.submitting.set(true);
    this.serverError.set(null);

    const dto = {
      
      firstName: this.firstName.value!,
      lastName: this.lastName.value!,
      username: this.username.value!,
      email: this.email.value!,
      password: this.password.value!,
      roles: this.form.value.roles ?? []
    };

    try {
      const res = await firstValueFrom(this.auth.register(dto));
      const token = res.accessToken ?? (res as any).token;
      if (!token) throw new Error('Kein Token erhalten.');

      this.auth.setToken(token);
      await this.router.navigate(['/tickets']);
    } catch (err: any) {
      const http = err as HttpErrorResponse;
      this.serverError.set(http.error?.message ?? err?.message ?? 'Registrierung fehlgeschlagen');
    } finally {
      this.submitting.set(false);
    }
  }
}
