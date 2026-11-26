import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer-service';


interface Employment {
  id?: string; employmentType: string; employerName?: string; designation?: string;
  employmentStartDate?: string; employmentEndDate?: string; currentlyEmployed?: boolean;
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
  readonly employmentTypes = ['FULL_TIME', 'PART_TIME', 'SELF_EMPLOYED', 'CONTRACT', 'FREELANCER', 'UNEMPLOYED', 'RETIRED', 'STUDENT', 'OTHER'];
  readonly message = signal('');
  readonly error = signal('');
  readonly saving = signal(false);
  readonly form = this.fb.nonNullable.group({
    employmentType: ['', Validators.required],
    employerName: [''],
    designation: [''],
    employmentStartDate: [''],
    employmentEndDate: [''],
    currentlyEmployed: [false]
  });
  constructor() {
    this.api.getEmployment<Employment>().subscribe({
      next: value => this.form.patchValue({
        employmentType: value.employmentType || '',
        employerName: value.employerName || '',
        designation: value.designation || '',
        employmentStartDate: value.employmentStartDate || '',
        employmentEndDate: value.employmentEndDate || '',
        currentlyEmployed: value.currentlyEmployed ?? false
      }),
      error: err => { if (err.status !== 404) this.error.set(err?.error?.message || 'Unable to load employment details.'); }
    });
  } label(value: string): string { return value.toLowerCase().replaceAll('_', ' ').replace(/\b\w/g, c => c.toUpperCase()); }
  employmentStatusChanged(): void {
    if (this.form.controls.currentlyEmployed.value) this.form.controls.employmentEndDate.setValue('');
  }
  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const value = this.form.getRawValue();
    if (value.employmentStartDate && value.employmentEndDate && value.employmentEndDate < value.employmentStartDate) {
      this.error.set('Employment end date cannot be earlier than the start date.'); return;
    }
    this.saving.set(true); this.error.set(''); this.message.set('');
    const body = {
      ...value,
      employmentEndDate: value.currentlyEmployed ? null : (value.employmentEndDate || null)
    };
    this.api.saveEmployment<Employment>(body).subscribe({
      next: () => { this.saving.set(false); this.message.set('Employment details saved successfully.'); },
      error: err => { this.saving.set(false); this.error.set(err?.error?.message || 'Unable to save employment details.'); }
    });
  }
}

