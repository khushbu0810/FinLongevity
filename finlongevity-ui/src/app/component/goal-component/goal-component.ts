import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { GoalsService } from '../../services/goals-service';

type GoalType = 'INDIVIDUAL' | 'GROUP' | 'HOME_PURCHASE' | 'TRAVEL' | 'EDUCATION' | 'EMERGENCY_FUND' | 'RETIREMENT' | 'OTHER';
type GoalStatus = 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'CANCELLED';
interface Goal { id: string; name: string; description?: string; goalType: GoalType; targetAmount: number; currentAmount: number; remainingAmount: number; progressPercentage: number; currency: string; targetDate?: string; status: GoalStatus; createdAt?: string; updatedAt?: string; }
interface Contribution { id: string; goalId: string; contributorUserId: number; amount: number; currency: string; status: 'PENDING' | 'CONFIRMED' | 'FAILED' | 'REFUNDED' | 'CANCELLED'; paymentReference?: string; note?: string; createdAt?: string; updatedAt?: string; }
interface Member { id: string; goalId: string; userId: number; role: 'OWNER' | 'CONTRIBUTOR'; status: string; joinedAt?: string; }
interface MemberAnalytics { userId: number; totalContributionAmount: number; confirmedAmount: number; pendingAmount: number; successfulContributionCount: number; failedContributionCount: number; contributionPercentage: number; }
interface Analytics { goalId: string; goalName: string; targetAmount: number; currentAmount: number; remainingAmount: number; progressPercentage: number; currency: string; totalContributions: number; confirmedContributions: number; pendingContributions: number; failedContributions: number; memberAnalytics: MemberAnalytics[]; }
@Component({
  standalone: true, imports: [ReactiveFormsModule, CurrencyPipe, DatePipe, DecimalPipe],
  selector: 'app-goal-component',
  styleUrl: './goal-component.css',
  templateUrl: './goal-component.html',
})
export class GoalComponent {
  private readonly fb = inject(FormBuilder); private readonly api = inject(GoalsService);
  readonly goalTypes: GoalType[] = ['INDIVIDUAL', 'GROUP', 'HOME_PURCHASE', 'TRAVEL', 'EDUCATION', 'EMERGENCY_FUND', 'RETIREMENT', 'OTHER'];
  readonly goals = signal<Goal[]>([]); readonly selected = signal<Goal | null>(null); readonly contributions = signal<Contribution[]>([]); readonly members = signal<Member[]>([]); readonly analytics = signal<Analytics | null>(null);
  readonly loading = signal(false); readonly saving = signal(false); readonly detailLoading = signal(false); readonly showForm = signal(false); readonly editingId = signal<string | null>(null); readonly showContribution = signal(false); readonly showMember = signal(false); readonly busyAction = signal(false);
  readonly error = signal(''); readonly message = signal(''); readonly filter = signal<'ALL' | GoalStatus>('ALL');
  readonly goalForm = this.fb.nonNullable.group({ name: ['', [Validators.required, Validators.maxLength(140)]], description: ['', [Validators.maxLength(1000)]], goalType: ['INDIVIDUAL' as GoalType, Validators.required], targetAmount: [1, [Validators.required, Validators.min(0.01)]], currency: ['INR', [Validators.required, Validators.pattern(/^[A-Za-z]{3}$/)]], targetDate: [''] });
  readonly contributionForm = this.fb.nonNullable.group({ amount: [1, [Validators.required, Validators.min(0.01)]], currency: ['INR', [Validators.required, Validators.pattern(/^[A-Za-z]{3}$/)]], note: ['', [Validators.maxLength(500)]] });
  readonly memberForm = this.fb.nonNullable.group({ userId: [0, [Validators.required, Validators.min(1)]] });
  constructor() { this.loadGoals(); }
  get filteredGoals(): Goal[] { const f = this.filter(); return this.goals().filter(g => f === 'ALL' || g.status === f); }
  get activeCount(): number { return this.goals().filter(g => g.status === 'ACTIVE').length; }
  get totalTarget(): number { return this.goals().filter(g => g.currency === 'INR' && g.status !== 'CANCELLED').reduce((s, g) => s + Number(g.targetAmount || 0), 0); }
  get totalSaved(): number { return this.goals().filter(g => g.currency === 'INR' && g.status !== 'CANCELLED').reduce((s, g) => s + Number(g.currentAmount || 0), 0); }
  loadGoals(): void { this.loading.set(true); this.error.set(''); this.api.getMyGoals<Goal[]>().subscribe({ next: r => { this.goals.set(r ?? []); this.loading.set(false); }, error: e => { this.loading.set(false); this.error.set(e?.error?.message || 'Unable to load financial goals. Check the Goals Service and API Gateway.'); } }); }
  openCreate(): void { this.editingId.set(null); this.goalForm.reset({ name: '', description: '', goalType: 'INDIVIDUAL', targetAmount: 1, currency: 'INR', targetDate: '' }); this.showForm.set(true); this.error.set(''); }
  openEdit(g: Goal): void { this.editingId.set(g.id); this.goalForm.reset({ name: g.name, description: g.description ?? '', goalType: g.goalType, targetAmount: g.targetAmount, currency: g.currency, targetDate: g.targetDate ?? '' }); this.showForm.set(true); this.error.set(''); }
  saveGoal(): void { if (this.goalForm.invalid) { this.goalForm.markAllAsTouched(); return; } const v = this.goalForm.getRawValue(); const body = { name: v.name.trim(), description: v.description.trim(), goalType: v.goalType, targetAmount: Number(v.targetAmount), currency: v.currency.toUpperCase(), ...(v.targetDate ? { targetDate: v.targetDate } : {}) }; this.saving.set(true); this.error.set(''); const id = this.editingId(); const req = id ? this.api.updateGoal<Goal>(id, body) : this.api.createGoal<Goal>(body); req.subscribe({ next: g => { this.saving.set(false); this.showForm.set(false); this.message.set(id ? 'Goal updated successfully.' : 'Goal created successfully.'); this.loadGoals(); if (id && this.selected()?.id === id) this.openDetails(g); }, error: e => { this.saving.set(false); this.error.set(e?.error?.message || 'Unable to save this goal.'); } }); }
  openDetails(g: Goal): void { this.selected.set(g); this.detailLoading.set(true); this.contributions.set([]); this.members.set([]); this.analytics.set(null); this.error.set(''); this.api.getGoal<Goal>(g.id).subscribe({ next: full => { this.selected.set(full); this.detailLoading.set(false); this.loadGoalSections(full.id, full.goalType === 'GROUP'); }, error: e => { this.detailLoading.set(false); this.error.set(e?.error?.message || 'Unable to load goal details.'); } }); }
  loadGoalSections(id: string, isGroup: boolean): void { this.api.getContributions<Contribution[]>(id).subscribe({ next: r => this.contributions.set(r ?? []), error: e => this.error.set(e?.error?.message || 'Could not load contributions.') }); if (isGroup) { this.api.getMembers<Member[]>(id).subscribe({ next: r => this.members.set(r ?? []), error: e => this.error.set(e?.error?.message || 'Could not load goal members.') }); this.api.getAnalytics<Analytics>(id).subscribe({ next: r => this.analytics.set(r), error: e => this.error.set(e?.error?.message || 'Could not load group analytics.') }); } }
  closeDetails(): void { this.selected.set(null); this.showContribution.set(false); this.showMember.set(false); }
  openContribution(): void { const g = this.selected(); if (!g) return; this.contributionForm.reset({ amount: 1, currency: g.currency, note: '' }); this.showContribution.set(true); }
  addContribution(): void { const g = this.selected(); if (!g || this.contributionForm.invalid) { this.contributionForm.markAllAsTouched(); return; } const v = this.contributionForm.getRawValue(); this.busyAction.set(true); this.api.createContribution<Contribution>(g.id, { amount: Number(v.amount), currency: v.currency.toUpperCase(), ...(v.note.trim() ? { note: v.note.trim() } : {}) }).subscribe({ next: r => { this.busyAction.set(false); this.showContribution.set(false); this.message.set(`Contribution recorded with status ${r.status}.`); this.openDetails(g); this.loadGoals(); }, error: e => { this.busyAction.set(false); this.error.set(e?.error?.message || 'Unable to add contribution.'); } }); }
  openMember(): void { this.memberForm.reset({ userId: 0 }); this.showMember.set(true); }
  addMember(): void { const g = this.selected(); if (!g || this.memberForm.invalid) { this.memberForm.markAllAsTouched(); return; } this.busyAction.set(true); this.api.addMember<Member>(g.id, { userId: Number(this.memberForm.getRawValue().userId) }).subscribe({ next: () => { this.busyAction.set(false); this.showMember.set(false); this.message.set('Member added to the goal.'); this.openDetails(g); }, error: e => { this.busyAction.set(false); this.error.set(e?.error?.message || 'Unable to add member.'); } }); }
  changeStatus(g: Goal, status: GoalStatus): void { this.busyAction.set(true); this.error.set(''); this.api.changeStatus<Goal>(g.id, status).subscribe({ next: updated => { this.busyAction.set(false); this.message.set(`Goal status changed to ${status}.`); this.loadGoals(); if (this.selected()?.id === g.id) this.selected.set(updated); }, error: e => { this.busyAction.set(false); this.error.set(e?.error?.message || 'Unable to update goal status.'); } }); }
  label(v: string): string { return (v || '').toLowerCase().split('_').map(x => x.charAt(0).toUpperCase() + x.slice(1)).join(' '); }
  statusClass(v: string): string { return v === 'ACTIVE' || v === 'COMPLETED' ? 'positive' : v === 'CANCELLED' ? 'negative' : 'neutral'; }
  progress(g: Goal): number { return Math.max(0, Math.min(100, Number(g.progressPercentage) || 0)); }
}

