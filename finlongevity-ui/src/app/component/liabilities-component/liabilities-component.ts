import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer-service';

type LiabilityCategory =
  | 'HOME_LOAN' | 'PERSONAL_LOAN' | 'CAR_LOAN' | 'EDUCATION_LOAN'
  | 'CREDIT_CARD' | 'BUSINESS_LOAN' | 'OTHER_DEBT';

interface Liability {
  id: string;
  name: string;
  category: LiabilityCategory;
  outstandingAmount: number;
  currency: string;
  annualInterestRate?: number | null;
  monthlyEmi?: number | null;
  nextPaymentDate?: string | null;
  notes?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

interface LiabilityRequest {
  name: string;
  category: LiabilityCategory;
  outstandingAmount: number;
  currency: string;
  annualInterestRate?: number;
  monthlyEmi?: number;
  nextPaymentDate?: string;
  notes?: string;
}

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe, DatePipe],
  selector: 'app-liabilities-component',
  styleUrl: './liabilities-component.css',
  templateUrl: './liabilities-component.html',
})
export class LiabilitiesComponent {
  private readonly fb = inject(FormBuilder);
  private readonly customerApi = inject(CustomerService);

  readonly categories: LiabilityCategory[] = [
    'HOME_LOAN', 'PERSONAL_LOAN', 'CAR_LOAN', 'EDUCATION_LOAN',
    'CREDIT_CARD', 'BUSINESS_LOAN', 'OTHER_DEBT'
  ];

  readonly liabilities = signal<Liability[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly deletingId = signal<string | null>(null);
  readonly showForm = signal(false);
  readonly editingId = signal<string | null>(null);
  readonly message = signal('');
  readonly error = signal('');
  readonly selectedCategory = signal('ALL');

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    category: ['' as LiabilityCategory | '', Validators.required],
    outstandingAmount: [0, [Validators.required, Validators.min(0)]],
    currency: ['INR', [Validators.required, Validators.pattern(/^[A-Z]{3}$/)]],
    annualInterestRate: [null as number | null, [Validators.min(0), Validators.max(100)]],
    monthlyEmi: [null as number | null, [Validators.min(0)]],
    nextPaymentDate: [''],
    notes: ['', Validators.maxLength(500)]
  });

  constructor() {
    this.loadLiabilities();
  }

  loadLiabilities(): void {
    this.loading.set(true);
    this.error.set('');
    this.customerApi.getLiabilities<Liability[]>().subscribe({
      next: items => {
        this.liabilities.set(items ?? []);
        this.loading.set(false);
      },
      error: err => {
        this.error.set(err?.error?.message || 'Could not load liabilities. Check the Customer Service and API Gateway.');
        this.loading.set(false);
      }
    });
  }

  get filteredLiabilities(): Liability[] {
    const category = this.selectedCategory();
    return category === 'ALL'
      ? this.liabilities()
      : this.liabilities().filter(item => item.category === category);
  }

  // Currency totals are deliberately limited to INR to avoid adding unlike currencies.
  get totalInrOutstanding(): number {
    return this.liabilities()
      .filter(item => item.currency === 'INR')
      .reduce((total, item) => total + Number(item.outstandingAmount || 0), 0);
  }

  get totalInrMonthlyEmi(): number {
    return this.liabilities()
      .filter(item => item.currency === 'INR')
      .reduce((total, item) => total + Number(item.monthlyEmi || 0), 0);
  }

  get currencyCount(): number {
    return new Set(this.liabilities().map(item => item.currency)).size;
  }

  formatCategory(category: string): string {
    return category.toLowerCase().replaceAll('_', ' ').replace(/\b\w/g, letter => letter.toUpperCase());
  }

  openCreateForm(): void {
    this.editingId.set(null);
    this.form.reset({
      name: '', category: '', outstandingAmount: 0, currency: 'INR',
      annualInterestRate: null, monthlyEmi: null, nextPaymentDate: '', notes: ''
    });
    this.error.set('');
    this.message.set('');
    this.showForm.set(true);
  }

  openEditForm(item: Liability): void {
    this.editingId.set(item.id);
    this.form.reset({
      name: item.name,
      category: item.category,
      outstandingAmount: item.outstandingAmount,
      currency: item.currency,
      annualInterestRate: item.annualInterestRate ?? null,
      monthlyEmi: item.monthlyEmi ?? null,
      nextPaymentDate: item.nextPaymentDate ?? '',
      notes: item.notes ?? ''
    });
    this.error.set('');
    this.message.set('');
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.editingId.set(null);
  }

  saveLiability(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const request: LiabilityRequest = {
      name: value.name.trim(),
      category: value.category as LiabilityCategory,
      outstandingAmount: Number(value.outstandingAmount),
      currency: value.currency.toUpperCase(),
      ...(value.annualInterestRate !== null ? { annualInterestRate: Number(value.annualInterestRate) } : {}),
      ...(value.monthlyEmi !== null ? { monthlyEmi: Number(value.monthlyEmi) } : {}),
      ...(value.nextPaymentDate ? { nextPaymentDate: value.nextPaymentDate } : {}),
      ...(value.notes.trim() ? { notes: value.notes.trim() } : {})
    };

    this.saving.set(true);
    this.error.set('');
    const id = this.editingId();
    const operation = id
      ? this.customerApi.updateLiability<Liability>(id, request)
      : this.customerApi.createLiability<Liability>(request);

    operation.subscribe({
      next: () => {
        this.saving.set(false);
        this.closeForm();
        this.message.set(id ? 'Liability updated successfully.' : 'Liability added successfully.');
        this.loadLiabilities();
      },
      error: err => {
        this.saving.set(false);
        this.error.set(err?.error?.message || 'Unable to save the liability. Please verify the entered values.');
      }
    });
  }

  deleteLiability(item: Liability): void {
    if (!confirm(`Delete “${item.name}” from your liabilities?`)) return;
    this.deletingId.set(item.id);
    this.error.set('');
    this.customerApi.deleteLiability(item.id).subscribe({
      next: () => {
        this.deletingId.set(null);
        this.message.set('Liability deleted successfully.');
        this.loadLiabilities();
      },
      error: err => {
        this.deletingId.set(null);
        this.error.set(err?.error?.message || 'Unable to delete this liability.');
      }
    });
  }
}

