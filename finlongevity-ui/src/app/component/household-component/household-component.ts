import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer-service';

type Relationship = 'SELF' | 'SPOUSE' | 'CHILD' | 'PARENT' | 'SIBLING' | 'GRANDPARENT' | 'GRANDCHILD' | 'OTHER';
type MemberRole = 'OWNER' | 'ADMIN' | 'MEMBER';
interface Household { id?: string; name: string; description?: string; currency: string; }
interface HouseholdMember { id: string; fullName: string; relationship: Relationship; role: MemberRole; dateOfBirth?: string; email?: string; phone?: string; identityUserId?: string; }

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  selector: 'app-household-component',
  styleUrl: './household-component.css',
  templateUrl: './household-component.html',
})
export class HouseholdComponent {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(CustomerService);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly deletingId = signal('');
  readonly error = signal('');
  readonly message = signal('');
  readonly household = signal<Household | null>(null);
  readonly members = signal<HouseholdMember[]>([]);
  readonly showMemberForm = signal(false);
  readonly editingMemberId = signal('');

  readonly relationships: { value: Relationship; label: string }[] = [
    { value: 'SELF', label: 'Self' }, { value: 'SPOUSE', label: 'Spouse' }, { value: 'CHILD', label: 'Child' },
    { value: 'PARENT', label: 'Parent' }, { value: 'SIBLING', label: 'Sibling' },
    { value: 'GRANDPARENT', label: 'Grandparent' }, { value: 'GRANDCHILD', label: 'Grandchild' }, { value: 'OTHER', label: 'Other' }
  ];
  readonly roles: MemberRole[] = ['OWNER', 'ADMIN', 'MEMBER'];

  readonly householdForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', Validators.maxLength(500)],
    currency: ['INR', [Validators.required, Validators.pattern(/^[A-Z]{3}$/)]]
  });
  readonly memberForm = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.maxLength(120)]],
    relationship: ['OTHER' as Relationship, Validators.required],
    role: ['MEMBER' as MemberRole, Validators.required],
    dateOfBirth: [''], email: ['', Validators.email], phone: [''], identityUserId: ['']
  });

  constructor() { this.load(); }

  load(): void {
    this.loading.set(true); this.error.set('');
    this.api.getHousehold<Household>().subscribe({
      next: value => { this.household.set(value); this.householdForm.patchValue({ name: value.name ?? '', description: value.description ?? '', currency: value.currency ?? 'INR' }); this.loadMembers(); },
      error: err => {
        if (err?.status === 404) { this.household.set(null); this.loadMembers(); }
        else { this.error.set(err?.error?.message || 'Could not load household details.'); this.loading.set(false); }
      }
    });
  }

  private loadMembers(): void {
    this.api.getHouseholdMembers<unknown>().subscribe({
      next: result => { this.members.set(this.toArray<HouseholdMember>(result)); this.loading.set(false); },
      error: err => { if (err?.status === 404) this.members.set([]); else this.error.set(err?.error?.message || 'Could not load household members.'); this.loading.set(false); }
    });
  }

  private toArray<T>(result: unknown): T[] {
    if (Array.isArray(result)) return result as T[];
    if (result && typeof result === 'object') {
      const value = result as Record<string, unknown>;
      for (const key of ['content', 'items', 'members', 'data']) if (Array.isArray(value[key])) return value[key] as T[];
    }
    return [];
  }

  saveHousehold(): void {
    if (this.householdForm.invalid) { this.householdForm.markAllAsTouched(); return; }
    this.saving.set(true); this.error.set(''); this.message.set('');
    const request = this.householdForm.getRawValue();
    const action = this.household() ? this.api.saveHousehold<Household>(request) : this.api.createHousehold<Household>(request);
    action.subscribe({
      next: value => { this.household.set(value ?? request); this.saving.set(false); this.message.set('Household details saved successfully.'); },
      error: err => { this.saving.set(false); this.error.set(err?.error?.message || 'Could not save household details.'); }
    });
  }

  openAddMember(): void { this.editingMemberId.set(''); this.memberForm.reset({ fullName: '', relationship: 'OTHER', role: 'MEMBER', dateOfBirth: '', email: '', phone: '', identityUserId: '' }); this.showMemberForm.set(true); }
  editMember(member: HouseholdMember): void {
    this.editingMemberId.set(member.id);
    this.memberForm.reset({ fullName: member.fullName, relationship: member.relationship, role: member.role, dateOfBirth: member.dateOfBirth ?? '', email: member.email ?? '', phone: member.phone ?? '', identityUserId: member.identityUserId ?? '' });
    this.showMemberForm.set(true);
  }
  closeMemberForm(): void { this.showMemberForm.set(false); this.editingMemberId.set(''); }
  saveMember(): void {
    if (this.memberForm.invalid) { this.memberForm.markAllAsTouched(); return; }
    this.saving.set(true); this.error.set(''); this.message.set('');
    const raw = this.memberForm.getRawValue();
    const request = Object.fromEntries(Object.entries(raw).filter(([, value]) => value !== ''));
    const id = this.editingMemberId();
    const action = id ? this.api.updateHouseholdMember<HouseholdMember>(id, request) : this.api.addHouseholdMember<HouseholdMember>(request);
    action.subscribe({ next: () => { this.saving.set(false); this.closeMemberForm(); this.message.set(id ? 'Household member updated.' : 'Household member added.'); this.loadMembers(); }, error: err => { this.saving.set(false); this.error.set(err?.error?.message || 'Could not save household member.'); } });
  }
  deleteMember(member: HouseholdMember): void {
    if (!globalThis.confirm(`Remove ${member.fullName} from this household?`)) return;
    this.deletingId.set(member.id); this.error.set('');
    this.api.deleteHouseholdMember(member.id).subscribe({ next: () => { this.deletingId.set(''); this.members.update(items => items.filter(item => item.id !== member.id)); this.message.set('Household member removed.'); }, error: err => { this.deletingId.set(''); this.error.set(err?.error?.message || 'Could not remove household member.'); } });
  }
  relationshipLabel(value: Relationship): string { return this.relationships.find(item => item.value === value)?.label ?? value; }
  trackMember(_: number, member: HouseholdMember): string { return member.id; }
}
