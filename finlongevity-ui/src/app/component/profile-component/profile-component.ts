import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer-service';

interface Profile {
  customerId: string; firstName: string; lastName: string; email: string;
  phone?: string; dateOfBirth?: string; gender?: string;
}
@Component({
  standalone: true,
  imports: [ReactiveFormsModule],
  selector: 'app-profile-component',
  styleUrl: './profile-component.css',
  templateUrl: './profile-component.html',
})
export class ProfileComponent {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(CustomerService);
  readonly profile = signal<Profile | null>(null);
  readonly message = signal('');
  readonly error = signal('');
  readonly saving = signal(false);
  readonly form = this.fb.nonNullable.group({
    firstName: ['', [Validators.required, Validators.maxLength(100)]],
    lastName: ['', [Validators.required, Validators.maxLength(100)]],
    phone: [''],
    dateOfBirth: [''],
    gender: ['']
  });
  constructor() {
    this.api.getProfile<Profile>().subscribe({
      next: p => { this.profile.set(p); this.form.patchValue(p); },
      error: err => { if (err.status !== 404) this.error.set(err?.error?.message || 'Could not load profile.'); }
    });
  }
  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true); this.message.set(''); this.error.set('');
    const request = this.form.getRawValue();
    const operation = this.profile() ? this.api.updateProfile<Profile>(request) : this.api.createProfile<Profile>(request);
    operation.subscribe({
      next: p => { this.profile.set(p); this.message.set('Your profile has been saved.'); this.saving.set(false); },
      error: err => { this.error.set(err?.error?.message || 'Unable to save your profile.'); this.saving.set(false); }
    });
  }
}
