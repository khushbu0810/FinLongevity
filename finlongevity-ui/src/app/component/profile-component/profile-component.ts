
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer-service';

interface Profile {
  customerId: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  dateOfBirth?: string;
  gender?: string;
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

  readonly loading = signal(true);
  readonly saving = signal(false);

  readonly showWelcome = signal(false);
  readonly showForm = signal(false);

  readonly message = signal('');
  readonly error = signal('');

  readonly form = this.fb.nonNullable.group({
    firstName: ['', [
      Validators.required,
      Validators.maxLength(100)
    ]],
    lastName: ['', [
      Validators.required,
      Validators.maxLength(100)
    ]],
    phone: [''],
    dateOfBirth: [''],
    gender: ['']
  });

  constructor() {
    this.loadProfile();
  }

  // Check whether the logged-in customer already has a profile.
  loadProfile(): void {

    this.loading.set(true);
    this.error.set('');
    this.message.set('');

    this.api.getProfile<Profile>().subscribe({

      next: savedProfile => {

        // Existing customer.
        this.profile.set(savedProfile);

        this.showWelcome.set(false);
        this.showForm.set(false);

        this.form.patchValue({
          firstName: savedProfile.firstName || '',
          lastName: savedProfile.lastName || '',
          phone: savedProfile.phone || '',
          dateOfBirth: this.formatDateForInput(
            savedProfile.dateOfBirth
          ),
          gender: savedProfile.gender || ''
        });

        this.loading.set(false);
      },

      error: err => {

        this.loading.set(false);

        if (err.status === 404) {

          // Profile genuinely does not exist.
          this.profile.set(null);
          this.showWelcome.set(true);
          this.showForm.set(false);

        } else {

          // Never treat authorization errors as a new customer.
          this.error.set(
            err?.error?.message ||
            'Unable to load your profile. Please try again.'
          );

          this.showWelcome.set(false);
          this.showForm.set(false);

        }
      }
    });
  }

  // Fresh customer clicks Create Profile.
  startProfileCreation(): void {

    if (this.profile()) {
      return;
    }

    this.form.reset({
      firstName: '',
      lastName: '',
      phone: '',
      dateOfBirth: '',
      gender: ''
    });

    this.error.set('');
    this.message.set('');

    this.showWelcome.set(false);
    this.showForm.set(true);
  }

  // Cancel create/edit.
  cancelForm(): void {

    this.showForm.set(false);
    this.error.set('');

    if (this.profile()) {
      this.showWelcome.set(false);
    } else {
      this.showWelcome.set(true);
    }
  }

  // Edit existing profile.
  editProfile(): void {

    const savedProfile = this.profile();

    if (!savedProfile) {
      return;
    }

    this.form.patchValue({
      firstName: savedProfile.firstName || '',
      lastName: savedProfile.lastName || '',
      phone: savedProfile.phone || '',
      dateOfBirth: this.formatDateForInput(
        savedProfile.dateOfBirth
      ),
      gender: savedProfile.gender || ''
    });

    this.message.set('');
    this.error.set('');

    this.showWelcome.set(false);
    this.showForm.set(true);
  }

  // Convert backend date to HTML date input format.
  private formatDateForInput(date?: string): string {

    if (!date) {
      return '';
    }

    if (/^\d{4}-\d{2}-\d{2}$/.test(date)) {
      return date;
    }

    const match = date.match(
      /^(\d{2})-(\d{2})-(\d{4})$/
    );

    if (match) {
      return `${match[3]}-${match[2]}-${match[1]}`;
    }

    return date.split('T')[0];
  }

  // Create or update profile.
  save(): void {

    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.error.set('');
    this.message.set('');

    const raw = this.form.getRawValue();
    const request = { ...raw, dateOfBirth: raw.dateOfBirth || null };

    // Existing profile = PUT.
    // Missing profile = POST.
    const isNewProfile = this.profile() === null;

    const operation = isNewProfile
      ? this.api.createProfile<Profile>(request)
      : this.api.updateProfile<Profile>(request);

    operation.subscribe({

      next: savedProfile => {

        this.profile.set(savedProfile);

        this.showWelcome.set(false);
        this.showForm.set(false);

        this.message.set(
          isNewProfile
            ? 'Your profile has been created successfully.'
            : 'Your profile has been updated successfully.'
        );

        // Keep saved values in the form.
        this.form.patchValue({
          firstName: savedProfile.firstName || '',
          lastName: savedProfile.lastName || '',
          phone: savedProfile.phone || '',
          dateOfBirth: this.formatDateForInput(
            savedProfile.dateOfBirth
          ),
          gender: savedProfile.gender || ''
        });

        this.saving.set(false);
      },

      error: err => {

        this.error.set(
          err?.error?.message ||
          'Unable to save your profile. Please try again.'
        );

        this.saving.set(false);

        // If backend reports a duplicate, retrieve existing profile.
        if (err.status === 409) {
          this.loadProfile();
        }
      }
    });
  }
}
