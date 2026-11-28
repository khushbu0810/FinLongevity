
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer-service';
import { AuthService } from '../../services/auth-service';

interface Employment {
  id?: string;
  employmentType: string;
  employerName?: string;
  designation?: string;
  employmentStartDate?: string;
  employmentEndDate?: string;
  currentlyEmployed?: boolean;
}

@Component({
  standalone: true,
  imports: [ReactiveFormsModule],
  selector: 'app-employment-component',
  styleUrl: './employment-component.css',
  templateUrl: './employment-component.html',
})
export class EmploymentComponent {

  private readonly fb = inject(FormBuilder);
  private readonly api = inject(CustomerService);
  private readonly auth = inject(AuthService);

  readonly employment = signal<Employment | null>(null);

  readonly employmentTypes = [
    'FULL_TIME',
    'PART_TIME',
    'SELF_EMPLOYED',
    'CONTRACT',
    'FREELANCER',
    'UNEMPLOYED',
    'RETIRED',
    'STUDENT',
    'OTHER'
  ];

  readonly loading = signal(false);
  readonly saving = signal(false);

  readonly showWelcome = signal(false);
  readonly showForm = signal(false);

  readonly message = signal('');
  readonly error = signal('');

  readonly form = this.fb.nonNullable.group({
    employmentType: ['', Validators.required],
    employerName: [''],
    designation: [''],
    employmentStartDate: [''],
    employmentEndDate: [''],
    currentlyEmployed: [false]
  });

  // Unique storage key for the logged-in customer.
  private getStorageKey(): string {

    const user = this.auth.currentUser() as
      { email?: string; userId?: string; id?: string } | null;

    const identity =
      user?.email ||
      user?.userId ||
      user?.id;

    return `finlongevity_employment_created_${identity || 'unknown'}`;
  }

  constructor() {
    this.initializeEmployment();
  }

  // INITIALIZE
  // Do not call GET for a fresh customer.
  initializeEmployment(): void {

    const created = localStorage.getItem(this.getStorageKey());

    if (created === 'true') {

      // Previously created employment.
      this.loadEmployment();

    } else {

      // Fresh customer: show popup without API call.
      this.employment.set(null);
      this.loading.set(false);

      this.showWelcome.set(true);
      this.showForm.set(false);
    }
  }

  // LOAD EXISTING EMPLOYMENT
  loadEmployment(): void {

    this.loading.set(true);
    this.error.set('');

    this.api.getEmployment<Employment>().subscribe({

      next: saved => {

        this.employment.set(saved);

        this.form.patchValue({
          employmentType: saved.employmentType || '',
          employerName: saved.employerName || '',
          designation: saved.designation || '',
          employmentStartDate: this.formatDate(
            saved.employmentStartDate
          ),
          employmentEndDate: this.formatDate(
            saved.employmentEndDate
          ),
          currentlyEmployed: saved.currentlyEmployed ?? false
        });

        this.showWelcome.set(false);
        this.showForm.set(false);

        this.loading.set(false);
      },

      error: err => {

        this.loading.set(false);

        if (err.status === 404) {

          // No record exists anymore.
          localStorage.removeItem(this.getStorageKey());

          this.employment.set(null);
          this.showWelcome.set(true);
          this.showForm.set(false);

        } else {

          this.error.set(
            err?.error?.message ||
            'Unable to load employment details. Please try again.'
          );
        }
      }
    });
  }

  // DATE FORMATTING
  private formatDate(date?: string): string {

    if (!date) return '';

    if (/^\d{4}-\d{2}-\d{2}$/.test(date)) {
      return date;
    }

    const match = date.match(/^(\d{2})-(\d{2})-(\d{4})$/);

    if (match) {
      return `${match[3]}-${match[2]}-${match[1]}`;
    }

    return date.split('T')[0];
  }

  // READABLE EMPLOYMENT TYPE
  label(value: string): string {

    return value
      .toLowerCase()
      .replaceAll('_', ' ')
      .replace(/\b\w/g, c => c.toUpperCase());
  }

  // START CREATION
  startEmploymentCreation(): void {

    if (this.employment()) return;

    this.form.reset({
      employmentType: '',
      employerName: '',
      designation: '',
      employmentStartDate: '',
      employmentEndDate: '',
      currentlyEmployed: false
    });

    this.error.set('');
    this.message.set('');

    this.showWelcome.set(false);
    this.showForm.set(true);
  }

  // CANCEL FORM
  cancelForm(): void {

    this.showForm.set(false);
    this.error.set('');

    if (this.employment()) {
      this.showWelcome.set(false);
    } else {
      this.showWelcome.set(true);
    }
  }

  // EDIT EXISTING EMPLOYMENT
  editEmployment(): void {

    const saved = this.employment();

    if (!saved) return;

    this.form.patchValue({
      employmentType: saved.employmentType || '',
      employerName: saved.employerName || '',
      designation: saved.designation || '',
      employmentStartDate: this.formatDate(saved.employmentStartDate),
      employmentEndDate: this.formatDate(saved.employmentEndDate),
      currentlyEmployed: saved.currentlyEmployed ?? false
    });

    this.error.set('');
    this.message.set('');

    this.showWelcome.set(false);
    this.showForm.set(true);
  }

  // EMPLOYMENT STATUS
  employmentStatusChanged(): void {

    if (this.form.controls.currentlyEmployed.value) {
      this.form.controls.employmentEndDate.setValue('');
    }
  }

  // CREATE OR UPDATE
  save(): void {

    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();

    if (
      value.employmentStartDate &&
      value.employmentEndDate &&
      value.employmentEndDate < value.employmentStartDate
    ) {
      this.error.set(
        'Employment end date cannot be earlier than the start date.'
      );
      return;
    }

    this.saving.set(true);
    this.error.set('');
    this.message.set('');

    const isNew = this.employment() === null;

    const body = {
      ...value,
      employmentStartDate: value.employmentStartDate || null,
      employmentEndDate: value.currentlyEmployed
        ? null
        : value.employmentEndDate || null
    };

    // Existing backend PUT endpoint.
    this.api.saveEmployment<Employment>(body).subscribe({

      next: saved => {

        // Mark employment as created for this customer.
        localStorage.setItem(this.getStorageKey(), 'true');

        this.employment.set(saved);

        this.saving.set(false);

        this.showWelcome.set(false);
        this.showForm.set(false);

        this.message.set(
          isNew
            ? 'Employment details created successfully.'
            : 'Employment details updated successfully.'
        );

        // GET is called ONLY AFTER successful creation/update.
        this.loadEmployment();
      },

      error: err => {

        this.error.set(
          err?.error?.message ||
          'Unable to save employment details. Please try again.'
        );

        this.saving.set(false);
      }
    });
  }
}
