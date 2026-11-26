import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PaymentService } from '../../services/payment-service';


type BusinessType = 'INVESTMENT_PURCHASE' | 'INVESTMENT_ADDITIONAL_CONTRIBUTION' | 'INVESTMENT_REDEMPTION' | 'INSURANCE_PREMIUM' | 'GOAL_CONTRIBUTION' | 'OTHER';
type PaymentMethod = 'UPI' | 'CREDIT_CARD' | 'DEBIT_CARD' | 'NET_BANKING' | 'WALLET' | 'BANK_TRANSFER' | 'MOCK';
type PaymentStatus = 'CREATED' | 'INITIATED' | 'PENDING' | 'SUCCESS' | 'FAILED' | 'CANCELLED' | 'REFUNDED' | 'PARTIALLY_REFUNDED';
interface Payment { id: string; paymentReference?: string; businessReference: string; businessType: BusinessType; amount: number; currency: string; status: PaymentStatus; paymentMethod: PaymentMethod; gatewayName?: string; gatewayTransactionId?: string; failureReason?: string; initiatedAt?: string; completedAt?: string; createdAt?: string; updatedAt?: string; }
interface PaymentRequest { businessReference: string; businessType: BusinessType; amount: number; currency: string; paymentMethod: PaymentMethod; }
interface PaymentEvent { id: string; paymentId: string; eventType: string; source?: string; externalEventId?: string; description?: string; createdAt?: string; }
@Component({
  standalone: true, imports: [ReactiveFormsModule, CurrencyPipe, DatePipe],
  selector: 'app-payments-component',
  styleUrl: './payments-component.css',
  templateUrl: './payments-component.html',
})
export class PaymentsComponent {
  private readonly fb = inject(FormBuilder); private readonly api = inject(PaymentService);
  readonly businessTypes: BusinessType[] = ['INVESTMENT_PURCHASE', 'INVESTMENT_ADDITIONAL_CONTRIBUTION', 'INVESTMENT_REDEMPTION', 'INSURANCE_PREMIUM', 'GOAL_CONTRIBUTION', 'OTHER'];
  readonly paymentMethods: PaymentMethod[] = ['UPI', 'CREDIT_CARD', 'DEBIT_CARD', 'NET_BANKING', 'WALLET', 'BANK_TRANSFER', 'MOCK'];
  readonly payments = signal<Payment[]>([]); readonly events = signal<PaymentEvent[]>([]); readonly selectedPayment = signal<Payment | null>(null);
  readonly loading = signal(false); readonly detailLoading = signal(false); readonly eventLoading = signal(false); readonly eventError = signal(''); readonly saving = signal(false); readonly processingId = signal<string | null>(null); readonly showForm = signal(false);
  readonly statusFilter = signal('ALL'); readonly methodFilter = signal('ALL'); readonly searchTerm = signal('');
  readonly message = signal(''); readonly error = signal('');
  readonly form = this.fb.nonNullable.group({ businessReference: ['', [Validators.required, Validators.maxLength(120)]], businessType: ['' as BusinessType | '', Validators.required], amount: [0, [Validators.required, Validators.min(0.01)]], currency: ['INR', [Validators.required, Validators.pattern(/^[A-Za-z]{3}$/)]], paymentMethod: ['' as PaymentMethod | '', Validators.required] });
  constructor() { this.loadPayments(); }
  loadPayments(): void { this.loading.set(true); this.error.set(''); this.api.getMyPayments<Payment[]>().subscribe({ next: rows => { this.payments.set(rows ?? []); this.loading.set(false); }, error: e => { this.error.set(e?.error?.message || 'Could not load payments. Check the Payment Service and API Gateway.'); this.loading.set(false); } }); }
  get filteredPayments(): Payment[] {
    const term = this.searchTerm().trim().toLowerCase();
    return this.payments().filter(p => (this.statusFilter() === 'ALL' || p.status === this.statusFilter()) && (this.methodFilter() === 'ALL' || p.paymentMethod === this.methodFilter()) && (!term || [p.paymentReference, p.id, p.businessReference, p.businessType].some(v => (v || '').toLowerCase().includes(term))));
  }
  get reconciliationEvents(): PaymentEvent[] { return this.events().filter(e => e.eventType === 'RECONCILIATION_UPDATED'); }
  get latestReconciliation(): PaymentEvent | null { return this.reconciliationEvents[0] ?? null; }
  get totalSuccessfulInr(): number { return this.payments().filter(p => p.currency === 'INR' && p.status === 'SUCCESS').reduce((s, p) => s + Number(p.amount || 0), 0); }
  get pendingCount(): number { return this.payments().filter(p => ['CREATED', 'INITIATED', 'PENDING'].includes(p.status)).length; }
  get failedCount(): number { return this.payments().filter(p => p.status === 'FAILED').length; }
  openCreateForm(): void { this.form.reset({ businessReference: '', businessType: '', amount: 0, currency: 'INR', paymentMethod: '' }); this.showForm.set(true); this.message.set(''); this.error.set(''); }
  createPayment(): void { if (this.form.invalid) { this.form.markAllAsTouched(); return; } const v = this.form.getRawValue(); const body: PaymentRequest = { businessReference: v.businessReference.trim(), businessType: v.businessType as BusinessType, amount: Number(v.amount), currency: v.currency.toUpperCase(), paymentMethod: v.paymentMethod as PaymentMethod }; this.saving.set(true); this.error.set(''); this.api.createPayment<Payment>(body).subscribe({ next: p => { this.saving.set(false); this.showForm.set(false); this.message.set(`Payment created${p?.paymentReference ? ` · ${p.paymentReference}` : ''}.`); this.loadPayments(); }, error: e => { this.saving.set(false); this.error.set(e?.error?.message || 'Unable to create payment. Verify the request details.'); } }); }
  viewDetails(payment: Payment): void { this.selectedPayment.set(payment); this.events.set([]); this.detailLoading.set(true); this.error.set(''); this.api.getPayment<Payment>(payment.id).subscribe({ next: p => { this.selectedPayment.set(p); this.detailLoading.set(false); this.loadEvents(p.id); }, error: e => { this.detailLoading.set(false); this.error.set(e?.error?.message || 'Could not load payment details.'); } }); }
  loadEvents(id: string): void {
    this.eventLoading.set(true); this.eventError.set('');
    this.api.getPaymentEvents<PaymentEvent[]>(id).subscribe({ next: rows => { const ordered = [...(rows ?? [])].sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime()); this.events.set(ordered); this.eventLoading.set(false); }, error: e => { this.eventError.set(e?.error?.message || 'Could not load payment events.'); this.eventLoading.set(false); } });
  }
  closeDetails(): void { this.selectedPayment.set(null); this.events.set([]); this.eventError.set(''); this.eventLoading.set(false); }
  clearFilters(): void { this.statusFilter.set('ALL'); this.methodFilter.set('ALL'); this.searchTerm.set(''); }
  processMock(payment: Payment, outcome: 'SUCCESS' | 'FAILED'): void { this.processingId.set(payment.id); this.error.set(''); this.message.set(''); this.api.processMockPayment<Payment>(payment.id, outcome).subscribe({ next: updated => { this.processingId.set(null); this.selectedPayment.set(updated); this.message.set(`Mock payment processed with outcome ${outcome}.`); this.loadPayments(); this.loadEvents(payment.id); }, error: e => { this.processingId.set(null); this.error.set(e?.error?.message || 'Mock payment processing failed.'); } }); }
  canProcess(p: Payment): boolean { return ['CREATED', 'INITIATED', 'PENDING'].includes(p.status); }
  formatLabel(value: string | null | undefined): string { return (value || '').toLowerCase().split('_').map(x => x.charAt(0).toUpperCase() + x.slice(1)).join(' '); }
  statusClass(status: string): string { if (['SUCCESS', 'REFUNDED'].includes(status)) return 'status-positive'; if (['FAILED', 'CANCELLED'].includes(status)) return 'status-negative'; return 'status-neutral'; }
}
