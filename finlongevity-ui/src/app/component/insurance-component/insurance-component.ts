import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { InsuranceService } from '../../services/insurance-service';

type InsuranceType = 'LIFE' | 'HEALTH' | 'TERM' | 'MOTOR' | 'TRAVEL' | 'HOME' | 'OTHER';
interface Application { id: string; providerProductId: string; productNameSnapshot: string; insuranceType: InsuranceType; applicantName: string; applicantDateOfBirth: string; coverageAmount: number; currency: string; status: 'DRAFT' | 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'WITHDRAWN'; submittedAt?: string; decisionAt?: string; decisionNote?: string; createdAt?: string; updatedAt?: string; }
interface Policy { id: string; applicationId: string; policyNumber: string; providerProductId: string; coverageAmount: number; premiumAmount: number; currency: string; premiumFrequency: 'MONTHLY' | 'QUARTERLY' | 'HALF_YEARLY' | 'YEARLY' | 'SINGLE'; status: 'ACTIVE' | 'PENDING_ACTIVATION' | 'LAPSED' | 'EXPIRED' | 'CANCELLED' | 'CLAIM_IN_PROGRESS' | 'CLOSED'; startDate?: string; endDate?: string; nextPremiumDueDate?: string; createdAt?: string; updatedAt?: string; }
@Component({
  standalone: true, imports: [ReactiveFormsModule, CurrencyPipe, DatePipe],
  selector: 'app-insurance-component',
  styleUrl: './insurance-component.css',
  templateUrl: './insurance-component.html',
})
export class InsuranceComponent {
  private readonly fb = inject(FormBuilder); private readonly api = inject(InsuranceService);
  readonly applications = signal<Application[]>([]); readonly policies = signal<Policy[]>([]); readonly selectedApplication = signal<Application | null>(null); readonly selectedPolicy = signal<Policy | null>(null);
  readonly loading = signal(false); readonly saving = signal(false); readonly submittingId = signal<string | null>(null); readonly showForm = signal(false); readonly activeTab = signal<'APPLICATIONS' | 'POLICIES'>('APPLICATIONS'); readonly error = signal(''); readonly message = signal('');
  readonly types: InsuranceType[] = ['LIFE', 'HEALTH', 'TERM', 'MOTOR', 'TRAVEL', 'HOME', 'OTHER'];
  get activePoliciesCount(): number { return this.policies().filter(p => p.status === 'ACTIVE').length; }
  get totalCoverageInr(): number { return this.policies().filter(p => p.currency === 'INR').reduce((s, p) => s + Number(p.coverageAmount || 0), 0); }
  readonly form = this.fb.nonNullable.group({ providerProductId: ['', [Validators.required]], productNameSnapshot: ['', [Validators.required, Validators.maxLength(180)]], insuranceType: ['HEALTH' as InsuranceType, Validators.required], applicantName: ['', [Validators.required, Validators.maxLength(150)]], applicantDateOfBirth: ['', [Validators.required]], coverageAmount: [1, [Validators.required, Validators.min(0.01)]], currency: ['INR', [Validators.required, Validators.pattern(/^[A-Za-z]{3}$/)]] });
  constructor() { this.loadAll(); }
  loadAll(): void { this.loading.set(true); this.error.set(''); let done = 0; const finish = () => { done++; if (done === 2) this.loading.set(false); }; this.api.getMyApplications<Application[]>().subscribe({ next: r => { this.applications.set(r ?? []); finish(); }, error: e => { this.error.set(e?.error?.message || 'Unable to load insurance applications.'); finish(); } }); this.api.getMyPolicies<Policy[]>().subscribe({ next: r => { this.policies.set(r ?? []); finish(); }, error: e => { this.error.set(e?.error?.message || 'Unable to load insurance policies.'); finish(); } }); }
  openCreate(): void { this.form.reset({ providerProductId: '', productNameSnapshot: '', insuranceType: 'HEALTH', applicantName: '', applicantDateOfBirth: '', coverageAmount: 1, currency: 'INR' }); this.showForm.set(true); this.error.set(''); }
  create(): void { if (this.form.invalid) { this.form.markAllAsTouched(); return; } const v = this.form.getRawValue(); const body = { providerProductId: v.providerProductId.trim(), productNameSnapshot: v.productNameSnapshot.trim(), insuranceType: v.insuranceType, applicantName: v.applicantName.trim(), applicantDateOfBirth: v.applicantDateOfBirth, coverageAmount: Number(v.coverageAmount), currency: v.currency.toUpperCase() }; this.saving.set(true); this.api.createApplication<Application>(body).subscribe({ next: a => { this.saving.set(false); this.showForm.set(false); this.message.set('Insurance application saved as ' + a.status + '.'); this.loadAll(); this.viewApplication(a); }, error: e => { this.saving.set(false); this.error.set(e?.error?.message || 'Unable to create insurance application. Verify the product UUID and details.'); } }); }
  viewApplication(a: Application): void { this.selectedPolicy.set(null); this.api.getApplication<Application>(a.id).subscribe({ next: r => this.selectedApplication.set(r), error: e => this.error.set(e?.error?.message || 'Unable to load application details.') }); }
  submit(a: Application): void { this.submittingId.set(a.id); this.api.submitApplication<Application>(a.id).subscribe({ next: r => { this.submittingId.set(null); this.message.set('Application submitted successfully.'); this.viewApplication(r); this.loadAll(); }, error: e => { this.submittingId.set(null); this.error.set(e?.error?.message || 'Unable to submit this application.'); } }); }
  viewPolicy(p: Policy): void { this.selectedApplication.set(null); this.api.getPolicy<Policy>(p.id).subscribe({ next: r => this.selectedPolicy.set(r), error: e => this.error.set(e?.error?.message || 'Unable to load policy details.') }); }
  closeDetails(): void { this.selectedApplication.set(null); this.selectedPolicy.set(null); }
  label(v: string): string { return (v || '').toLowerCase().split('_').map(x => x.charAt(0).toUpperCase() + x.slice(1)).join(' '); }
  statusClass(v: string): string { return ['ACTIVE', 'APPROVED'].includes(v) ? 'positive' : ['REJECTED', 'LAPSED', 'EXPIRED', 'CANCELLED'].includes(v) ? 'negative' : 'neutral'; }
}