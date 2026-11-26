import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer-service';

interface Expense {
  id: string;
  name: string;
  category: string;
  amount: number;
  currency: string;
  frequency: string;
  startDate: string;
  endDate?: string | null;
  essential?: boolean;
  active?: boolean;
  notes?: string;
}

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe, DatePipe],
  selector: 'app-expense-component',
  styleUrl: './expense-component.css',
  templateUrl: './expense-component.html',
})
export class ExpenseComponent {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(CustomerService);

  readonly categories = [
    'HOUSING', 'UTILITIES', 'GROCERIES', 'TRANSPORTATION', 'HEALTHCARE',
    'EDUCATION', 'INSURANCE_PREMIUM', 'LOAN_EMI', 'SUBSCRIPTIONS',
    'ENTERTAINMENT', 'TRAVEL', 'CHILDCARE', 'TAXES', 'PERSONAL_CARE',
    'SHOPPING', 'OTHER'
  ];
  readonly frequencies = ['ONE_TIME', 'WEEKLY', 'BIWEEKLY', 'MONTHLY', 'QUARTERLY', 'ANNUALLY', 'IRREGULAR'];
  readonly expenses = signal<Expense[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly showForm = signal(false);
  readonly editingId = signal<string | null>(null);
  readonly filter = signal<'ALL' | 'ACTIVE' | 'INACTIVE'>('ALL');
  readonly message = signal('');
  readonly error = signal('');

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    category: ['', Validators.required],
    amount: [0, [Validators.required, Validators.min(0.01)]],
    currency: ['INR', [Validators.required, Validators.pattern(/^[A-Za-z]{3}$/)]],
    frequency: ['', Validators.required],
    startDate: ['', Validators.required],
    endDate: [''],
    essential: [false],
    active: [true],
    notes: ['', Validators.maxLength(500)]
  });

  constructor() { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.api.getExpenses<Expense[]>().subscribe({
      next: rows => { this.expenses.set(rows ?? []); this.loading.set(false); },
      error: err => {
        this.error.set(err?.error?.message || 'Unable to load expenses. Check that the Customer Service is running.');
        this.loading.set(false);
      }
    });
  }

  filteredExpenses(): Expense[] {
    const rows = this.expenses();
    if (this.filter() === 'ACTIVE') return rows.filter(x => x.active !== false);
    if (this.filter() === 'INACTIVE') return rows.filter(x => x.active === false);
    return rows;
  }

  monthlyEquivalent(): number {
    return this.expenses().filter(x => x.active !== false).reduce((total, item) => {
      const factors: Record<string, number> = {
        WEEKLY: 52 / 12, BIWEEKLY: 26 / 12, MONTHLY: 1,
        QUARTERLY: 1 / 3, ANNUALLY: 1 / 12
      };
      return total + item.amount * (factors[item.frequency] ?? 0);
    }, 0);
  }

  essentialMonthlyEquivalent(): number {
    return this.expenses().filter(x => x.active !== false && x.essential).reduce((total, item) => {
      const factors: Record<string, number> = {
        WEEKLY: 52 / 12, BIWEEKLY: 26 / 12, MONTHLY: 1,
        QUARTERLY: 1 / 3, ANNUALLY: 1 / 12
      };
      return total + item.amount * (factors[item.frequency] ?? 0);
    }, 0);
  }

  label(value: string | null | undefined): string {
    return (value || '').toLowerCase().replaceAll('_', ' ').replace(/\b\w/g, c => c.toUpperCase());
  }

  newExpense(): void {
    this.editingId.set(null);
    this.form.reset({
      name: '', category: '', amount: 0, currency: 'INR', frequency: '',
      startDate: new Date().toISOString().slice(0, 10), endDate: '',
      essential: false, active: true, notes: ''
    });
    this.showForm.set(true);
    this.message.set('');
    this.error.set('');
  }

  edit(item: Expense): void {
    this.editingId.set(item.id);
    this.form.reset({
      name: item.name, category: item.category, amount: item.amount,
      currency: item.currency, frequency: item.frequency, startDate: item.startDate,
      endDate: item.endDate ?? '', essential: !!item.essential,
      active: item.active !== false, notes: item.notes ?? ''
    });
    this.showForm.set(true);
    this.message.set('');
    this.error.set('');
  }

  cancelForm(): void {
    this.showForm.set(false);
    this.editingId.set(null);
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const raw = this.form.getRawValue();
    if (raw.endDate && raw.endDate < raw.startDate) {
      this.error.set('End date cannot be earlier than start date.');
      return;
    }

    this.saving.set(true);
    this.error.set('');
    this.message.set('');
    const body = {
      ...raw,
      currency: raw.currency.toUpperCase(),
      endDate: raw.endDate || null
    };
    const id = this.editingId();
    const request = id
      ? this.api.updateExpense<Expense>(id, body)
      : this.api.createExpense<Expense>(body);

    request.subscribe({
      next: () => {
        this.saving.set(false);
        this.cancelForm();
        this.message.set(id ? 'Expense updated successfully.' : 'Expense added successfully.');
        this.load();
      },
      error: err => {
        this.saving.set(false);
        this.error.set(err?.error?.message || 'Unable to save this expense.');
      }
    });
  }

  remove(item: Expense): void {
    if (!confirm(`Delete "${item.name}"? This cannot be undone.`)) return;
    this.api.deleteExpense(item.id).subscribe({
      next: () => { this.message.set('Expense deleted.'); this.load(); },
      error: err => this.error.set(err?.error?.message || 'Unable to delete this expense.')
    });
  }
}

