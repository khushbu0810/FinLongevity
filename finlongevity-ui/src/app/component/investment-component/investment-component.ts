import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { InvestmentService } from '../../services/investment-service';
import { PaymentService } from '../../services/payment-service';
import { InvestmentDraftService, SelectedCatalogueProduct } from '../../services/investment-draft-service';

type ProductType = 'FIXED_DEPOSIT' | 'MUTUAL_FUND' | 'STOCK' | 'BOND' | 'OTHER';
type InvestmentStatus = 'DRAFT' | 'PENDING_PAYMENT' | 'ACTIVE' | 'PARTIALLY_REDEEMED' | 'REDEMPTION_PENDING' | 'REDEEMED' | 'CANCELLED' | 'FAILED';
type TransactionType = 'PURCHASE' | 'ADDITIONAL_INVESTMENT' | 'PARTIAL_REDEMPTION' | 'FULL_REDEMPTION' | 'INTEREST_CREDIT' | 'DIVIDEND_CREDIT' | 'FEE' | 'REVERSAL';

interface Investment {
  id: string; providerProductId: string; productNameSnapshot: string; productType: ProductType;
  investedAmount: number; currency: string; units?: number | null; status: InvestmentStatus;
  goalId?: string | null; openedAt?: string | null; createdAt?: string; updatedAt?: string;
}
interface InvestmentRequest {
  providerProductId: string; productNameSnapshot: string; productType: ProductType;
  investedAmount: number; currency: string; units?: number; goalId?: string;
}
interface InvestmentTransaction {
  id: string; investmentId: string; transactionType: TransactionType;
  status: 'INITIATED' | 'PENDING' | 'SUCCESS' | 'FAILED' | 'REVERSED';
  amount: number; currency?: string; units?: number; paymentReference?: string;
  description?: string; processedAt?: string; createdAt?: string;
}
interface TransactionRequest {
  transactionType: TransactionType; amount: number; currency?: string;
  units?: number; description?: string;
}
type PaymentStatus = 'CREATED' | 'INITIATED' | 'PENDING' | 'SUCCESS' | 'FAILED' | 'CANCELLED' | 'REFUNDED' | 'PARTIALLY_REFUNDED';
interface LinkedPayment {
  id: string; paymentReference?: string; businessReference: string; businessType: string;
  amount: number; currency: string; status: PaymentStatus; paymentMethod: string;
  gatewayName?: string; gatewayTransactionId?: string; failureReason?: string;
  initiatedAt?: string; completedAt?: string; createdAt?: string;
}

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe, DatePipe],
  selector: 'app-investment-component',
  styleUrl: './investment-component.css',
  templateUrl: './investment-component.html',
})
export class InvestmentComponent {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(InvestmentService);
  private readonly paymentApi = inject(PaymentService);
  private readonly investmentDraft = inject(InvestmentDraftService);

  readonly productTypes: ProductType[] = ['FIXED_DEPOSIT', 'MUTUAL_FUND', 'STOCK', 'BOND', 'OTHER'];
  readonly transactionTypes: TransactionType[] = ['PURCHASE', 'ADDITIONAL_INVESTMENT', 'PARTIAL_REDEMPTION', 'FULL_REDEMPTION', 'INTEREST_CREDIT', 'DIVIDEND_CREDIT', 'FEE', 'REVERSAL'];
  readonly investments = signal<Investment[]>([]);
  readonly transactions = signal<InvestmentTransaction[]>([]);
  readonly linkedPayment = signal<LinkedPayment | null>(null);
  readonly paymentBusy = signal(false);
  readonly selectedInvestment = signal<Investment | null>(null);
  readonly loading = signal(false);
  readonly detailLoading = signal(false);
  readonly saving = signal(false);
  readonly showForm = signal(false);
  readonly showTransactionForm = signal(false);
  readonly cancellingId = signal<string | null>(null);
  readonly message = signal('');
  readonly error = signal('');
  readonly selectedCatalogueProduct = signal<SelectedCatalogueProduct | null>(null);

  readonly form = this.fb.nonNullable.group({
    providerProductId: ['', [Validators.required, Validators.pattern(/^[0-9a-fA-F-]{36}$/)]],
    productNameSnapshot: ['', [Validators.required, Validators.maxLength(180)]],
    productType: ['' as ProductType | '', Validators.required],
    investedAmount: [0, [Validators.required, Validators.min(0.01)]],
    currency: ['INR', [Validators.required, Validators.pattern(/^[A-Za-z]{3}$/)]],
    units: [null as number | null, Validators.min(0.0000000001)],
    goalId: ['', Validators.pattern(/^$|^[0-9a-fA-F-]{36}$/)]
  });
  readonly transactionForm = this.fb.nonNullable.group({
    transactionType: ['' as TransactionType | '', Validators.required],
    amount: [0, [Validators.required, Validators.min(0.01)]],
    currency: ['INR', Validators.pattern(/^[A-Za-z]{3}$/)],
    units: [null as number | null, Validators.min(0.0000000001)],
    description: ['', Validators.maxLength(1000)]
  });

  constructor() {
    this.loadInvestments();
    const selected = this.investmentDraft.consumeSelectedProduct();
    if (selected) {
      this.selectedCatalogueProduct.set(selected);
      const minimum = Math.max(0.01, Number(selected.minimumInvestment || 0));
      const amountValidators = [Validators.required, Validators.min(minimum)];
      if (selected.maximumInvestment != null) amountValidators.push(Validators.max(Number(selected.maximumInvestment)));
      this.form.controls.investedAmount.setValidators(amountValidators);
      this.form.controls.investedAmount.updateValueAndValidity();
      this.form.reset({ providerProductId: selected.id, productNameSnapshot: selected.name, productType: selected.productType, investedAmount: selected.minimumInvestment, currency: selected.currency.toUpperCase(), units: null, goalId: '' });
      this.showForm.set(true);
      this.message.set(`Selected catalogue product: ${selected.name}. Review the amount and complete your investment record.`);
    }
  }

  loadInvestments(): void {
    this.loading.set(true); this.error.set('');
    this.api.getMyInvestments<Investment[]>().subscribe({
      next: items => { this.investments.set(items ?? []); this.loading.set(false); },
      error: err => { this.error.set(err?.error?.message || 'Could not load investments. Check the Investment Service and API Gateway.'); this.loading.set(false); }
    });
  }

  get totalInvestedInr(): number {
    return this.investments().filter(i => i.currency === 'INR' && !['CANCELLED', 'FAILED'].includes(i.status))
      .reduce((sum, i) => sum + Number(i.investedAmount || 0), 0);
  }
  get activeCount(): number { return this.investments().filter(i => ['ACTIVE', 'PARTIALLY_REDEEMED'].includes(i.status)).length; }
  get pendingCount(): number { return this.investments().filter(i => ['DRAFT', 'PENDING_PAYMENT', 'REDEMPTION_PENDING'].includes(i.status)).length; }

  openCreateForm(): void {
    this.selectedCatalogueProduct.set(null);
    this.form.controls.investedAmount.setValidators([Validators.required, Validators.min(0.01)]);
    this.form.controls.investedAmount.updateValueAndValidity();
    this.form.reset({ providerProductId: '', productNameSnapshot: '', productType: '', investedAmount: 0, currency: 'INR', units: null, goalId: '' });
    this.showForm.set(true); this.error.set(''); this.message.set('');
  }
  closeCreateForm(): void { this.showForm.set(false); }

  createInvestment(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    const request: InvestmentRequest = {
      providerProductId: v.providerProductId.trim(), productNameSnapshot: v.productNameSnapshot.trim(),
      productType: v.productType as ProductType, investedAmount: Number(v.investedAmount), currency: v.currency.toUpperCase(),
      ...(v.units !== null ? { units: Number(v.units) } : {}), ...(v.goalId.trim() ? { goalId: v.goalId.trim() } : {})
    };
    this.saving.set(true); this.error.set('');
    this.api.createInvestment<Investment>(request).subscribe({
      next: created => { this.saving.set(false); this.showForm.set(false); this.message.set(`Investment created${created?.id ? ` · ${created.id}` : ''}. Open its details to create and process the linked payment.`); this.loadInvestments(); if (created?.id) this.viewDetails(created); },
      error: err => { this.saving.set(false); this.error.set(err?.error?.message || 'Unable to create the investment. Verify the product UUID and request details.'); }
    });
  }

  viewDetails(investment: Investment): void {
    this.selectedInvestment.set(investment); this.transactions.set([]); this.linkedPayment.set(null); this.detailLoading.set(true); this.error.set('');
    this.api.getInvestment<Investment>(investment.id).subscribe({
      next: item => { this.selectedInvestment.set(item); this.detailLoading.set(false); this.loadTransactions(item.id); this.loadLinkedPayment(item.id); },
      error: err => { this.detailLoading.set(false); this.error.set(err?.error?.message || 'Could not load investment details.'); }
    });
  }
  closeDetails(): void { this.selectedInvestment.set(null); this.transactions.set([]); this.linkedPayment.set(null); this.showTransactionForm.set(false); }

  loadLinkedPayment(investmentId: string): void {
    this.paymentApi.getMyPayments<LinkedPayment[]>().subscribe({
      next: rows => {
        const matches = (rows ?? []).filter(p => p.businessReference === investmentId && p.businessType === 'INVESTMENT_PURCHASE');
        matches.sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime());
        this.linkedPayment.set(matches[0] ?? null);
      },
      error: err => this.error.set(err?.error?.message || 'Could not check linked payment records.')
    });
  }

  createInvestmentPayment(): void {
    const investment = this.selectedInvestment();
    if (!investment || this.paymentBusy()) return;
    if (!['DRAFT', 'PENDING_PAYMENT'].includes(investment.status)) {
      this.error.set('A purchase payment can only be initiated for a draft or payment-pending investment.'); return;
    }
    const existing = this.linkedPayment();
    if (existing && !['FAILED', 'CANCELLED'].includes(existing.status)) {
      this.error.set('A non-failed payment already exists for this investment. Open that payment below instead of creating a duplicate.'); return;
    }
    this.paymentBusy.set(true); this.error.set(''); this.message.set('');
    this.paymentApi.createPayment<LinkedPayment>({
      businessReference: investment.id, businessType: 'INVESTMENT_PURCHASE',
      amount: Number(investment.investedAmount), currency: investment.currency, paymentMethod: 'MOCK'
    }).subscribe({
      next: payment => { this.linkedPayment.set(payment); this.paymentBusy.set(false); this.message.set('Linked mock payment created. Choose a simulation outcome below.'); },
      error: err => { this.paymentBusy.set(false); this.error.set(err?.error?.message || 'Could not create the investment payment.'); }
    });
  }

  processInvestmentPayment(outcome: 'SUCCESS' | 'FAILED'): void {
    const payment = this.linkedPayment(); const investment = this.selectedInvestment();
    if (!payment || !investment || this.paymentBusy() || !['CREATED', 'INITIATED', 'PENDING'].includes(payment.status)) return;
    this.paymentBusy.set(true); this.error.set(''); this.message.set('');
    this.paymentApi.processMockPayment<LinkedPayment>(payment.id, outcome).subscribe({
      next: updated => {
        this.linkedPayment.set(updated); this.paymentBusy.set(false);
        this.message.set(`Mock payment simulation returned ${outcome}. Refreshing investment state from the Investment Service.`);
        this.refreshInvestmentAfterPayment(investment.id);
      },
      error: err => { this.paymentBusy.set(false); this.error.set(err?.error?.message || 'Mock payment processing failed.'); }
    });
  }

  refreshInvestmentAfterPayment(investmentId: string): void {
    this.api.getInvestment<Investment>(investmentId).subscribe({
      next: updated => { this.selectedInvestment.set(updated); this.loadInvestments(); this.loadTransactions(investmentId); },
      error: err => this.error.set(err?.error?.message || 'Payment was processed, but investment status could not be refreshed. Use Refresh to check again.')
    });
  }

  refreshLinkedPayment(): void { const id = this.selectedInvestment()?.id; if (id) this.loadLinkedPayment(id); }
  paymentStatusClass(status: string): string { return ['SUCCESS', 'REFUNDED'].includes(status) ? 'status-positive' : ['FAILED', 'CANCELLED'].includes(status) ? 'status-negative' : 'status-neutral'; }
  canCreatePayment(investment: Investment): boolean { return ['DRAFT', 'PENDING_PAYMENT'].includes(investment.status); }
  canProcessPayment(payment: LinkedPayment): boolean { return ['CREATED', 'INITIATED', 'PENDING'].includes(payment.status); }

  loadTransactions(investmentId: string): void {
    this.api.getTransactions<InvestmentTransaction[]>(investmentId).subscribe({
      next: items => this.transactions.set(items ?? []),
      error: err => this.error.set(err?.error?.message || 'Could not load investment transactions.')
    });
  }

  openTransactionForm(): void {
    const investment = this.selectedInvestment(); if (!investment) return;
    this.transactionForm.reset({ transactionType: '', amount: 0, currency: investment.currency, units: null, description: '' });
    this.showTransactionForm.set(true);
  }
  addTransaction(): void {
    const investment = this.selectedInvestment();
    if (!investment || this.transactionForm.invalid) { this.transactionForm.markAllAsTouched(); return; }
    const v = this.transactionForm.getRawValue();
    const request: TransactionRequest = {
      transactionType: v.transactionType as TransactionType, amount: Number(v.amount),
      ...(v.currency.trim() ? { currency: v.currency.toUpperCase() } : {}),
      ...(v.units !== null ? { units: Number(v.units) } : {}), ...(v.description.trim() ? { description: v.description.trim() } : {})
    };
    this.saving.set(true); this.error.set('');
    this.api.addTransaction<InvestmentTransaction>(investment.id, request).subscribe({
      next: () => { this.saving.set(false); this.showTransactionForm.set(false); this.message.set('Investment transaction submitted.'); this.loadTransactions(investment.id); this.loadInvestments(); },
      error: err => { this.saving.set(false); this.error.set(err?.error?.message || 'Unable to add this transaction.'); }
    });
  }

  cancelDraft(investment: Investment): void {
    if (investment.status !== 'DRAFT' || !confirm(`Cancel draft investment “${investment.productNameSnapshot}”?`)) return;
    this.cancellingId.set(investment.id); this.error.set('');
    this.api.cancelDraft<Investment>(investment.id).subscribe({
      next: () => { this.cancellingId.set(null); this.message.set('Draft investment cancelled.'); this.loadInvestments(); if (this.selectedInvestment()?.id === investment.id) this.viewDetails({ ...investment, status: 'CANCELLED' }); },
      error: err => { this.cancellingId.set(null); this.error.set(err?.error?.message || 'Unable to cancel this draft investment.'); }
    });
  }

  formatLabel(value: string): string { return value.toLowerCase().replaceAll('_', ' ').replace(/\b\w/g, c => c.toUpperCase()); }
  statusClass(status: string): string { return ['ACTIVE', 'SUCCESS'].includes(status) ? 'status-positive' : ['FAILED', 'CANCELLED'].includes(status) ? 'status-negative' : 'status-neutral'; }
}
