import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth-service';

@Component({
  imports: [ReactiveFormsModule, RouterLink],
  standalone: true,
  selector: 'app-register',
  styleUrl: './register.css',
  templateUrl: './register.html',
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly form = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]]
  });
  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true); this.error.set('');
    this.auth.register(this.form.getRawValue()).subscribe({
      next: () => this.auth.login({ email: this.form.controls.email.value, password: this.form.controls.password.value }).subscribe({
        next: () => this.router.navigate(['/dashboard']),
        error: err => { this.error.set(err?.error?.message || 'Account created. Please sign in.'); this.loading.set(false); this.router.navigate(['/login']); }
      }),
      error: err => { this.error.set(err?.error?.message || 'Unable to create account. Please try again.'); this.loading.set(false); }
    });
  }
}
