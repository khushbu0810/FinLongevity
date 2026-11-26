import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer-service';

interface IncomeSource {
  id: string;
  incomeType: string;
  sourceName: string;
  amount: number;
  frequency: string;
  currency: string;
  active: boolean;
  createdAt?: string;
}
@Component({
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe], selector: 'app-income-component',
  styleUrl: './income-component.css',
  templateUrl: './income-component.html',
})
export class IncomeComponent {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(CustomerService);
  readonly incomeTypes = ['SALARY', 'BUSINESS', 'FREELANCE', 'RENTAL', 'INTEREST', 'DIVIDEND', 'PENSION', 'OTHER'];
  readonly frequencies = ['WEEKLY', 'BIWEEKLY', 'MONTHLY', 'QUARTERLY', 'ANNUALLY', 'IRREGULAR'];
  readonly sources = signal<IncomeSource[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly showForm = signal(false);
  readonly editingId = signal<string | null>(null);
  readonly message = signal('');
  readonly error = signal('');
  readonly form = this.fb.nonNullable.group({
    incomeType: ['', Validators.required],
    sourceName: ['', [Validators.required, Validators.maxLength(120)]],
    amount: [0, [Validators.required, Validators.min(0.01)]],
    frequency: ['', Validators.required],
    currency: ['INR', [Validators.required, Validators.pattern(/^[A-Z]{3}$/)]],
    active: [true]
  });
  constructor() { this.load(); }
  load(): void {
    this.loading.set(true); this.error.set('');
    this.api.getIncomeSources<IncomeSource[]>().subscribe({
      next: rows => { this.sources.set(rows || []); this.loading.set(false); },
      error: err => { this.error.set(err?.error?.message || 'Unable to load income sources.'); this.loading.set(false); }
    });
  }
  activeCount(): number { return this.sources().filter(x => x.active).length; }
  monthlyEquivalent(): number {
    return this.sources().filter(x => x.active).reduce((sum, x) => {
      const factor: Record<string, number> = { WEEKLY: 52 / 12, BIWEEKLY: 26 / 12, MONTHLY: 1, QUARTERLY: 1 / 3, ANNUALLY: 1 / 12 };
      return sum + x.amount * (factor[x.frequency] || 0);
    }, 0);
  }
  label(value: string): string { return value.toLowerCase().replaceAll('_', ' ').replace(/\b\w/g, c => c.toUpperCase()); }
  newIncome(): void { this.editingId.set(null); this.form.reset({ incomeType: '', sourceName: '', amount: 0, frequency: '', currency: 'INR', active: true }); this.showForm.set(true); }
  edit(item: IncomeSource): void {
    this.editingId.set(item.id);
    this.form.patchValue({ incomeType: item.incomeType, sourceName: item.sourceName, amount: item.amount, frequency: item.frequency, currency: item.currency, active: item.active });
    this.showForm.set(true);
  }
  cancelForm(): void { this.showForm.set(false); this.editingId.set(null); }
  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true); this.error.set(''); this.message.set('');
    const body = { ...this.form.getRawValue(), currency: this.form.controls.currency.value.toUpperCase() };
    const id = this.editingId();
    const request = id ? this.api.updateIncome<IncomeSource>(id, body) : this.api.addIncome<IncomeSource>(body);
    request.subscribe({
      next: () => { this.saving.set(false); this.cancelForm(); this.message.set(id ? 'Income source updated.' : 'Income source added.'); this.load(); },
      error: err => { this.saving.set(false); this.error.set(err?.error?.message || 'Unable to save this income source.'); }
    });
  }
  remove(item: IncomeSource): void {
    if (!confirm(`Delete income source "${item.sourceName}"?`)) return;
    this.api.deleteIncome(item.id).subscribe({
      next: () => { this.message.set('Income source deleted.'); this.load(); },
      error: err => this.error.set(err?.error?.message || 'Unable to delete this income source.')
    });
  }
}