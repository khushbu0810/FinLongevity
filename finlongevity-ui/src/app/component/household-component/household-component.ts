
import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer-service';
import { AuthService } from '../../services/auth-service';

type Relationship =
  | 'SELF' | 'SPOUSE' | 'CHILD' | 'PARENT'
  | 'SIBLING' | 'GRANDPARENT' | 'GRANDCHILD' | 'OTHER';

type MemberRole = 'OWNER' | 'ADMIN' | 'MEMBER';

interface Household {
  id?: string;
  name: string;
  description?: string;
  currency: string;
}

interface HouseholdMember {
  id: string;
  fullName: string;
  relationship: Relationship;
  role: MemberRole;
  dateOfBirth?: string;
  email?: string;
  phone?: string;
  identityUserId?: string;
}

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
  private readonly auth = inject(AuthService);

  // State
  readonly household = signal<Household | null>(null);
  readonly members = signal<HouseholdMember[]>([]);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly deletingId = signal('');

  readonly showWelcome = signal(false);
  readonly showHouseholdForm = signal(false);
  readonly showMemberForm = signal(false);

  readonly editingMemberId = signal('');
  readonly message = signal('');
  readonly error = signal('');

  // Relationship options
  readonly relationships: {
    value: Relationship;
    label: string;
  }[] = [
      { value: 'SELF', label: 'Self' },
      { value: 'SPOUSE', label: 'Spouse' },
      { value: 'CHILD', label: 'Child' },
      { value: 'PARENT', label: 'Parent' },
      { value: 'SIBLING', label: 'Sibling' },
      { value: 'GRANDPARENT', label: 'Grandparent' },
      { value: 'GRANDCHILD', label: 'Grandchild' },
      { value: 'OTHER', label: 'Other' }
    ];

  readonly roles: MemberRole[] = [
    'OWNER',
    'ADMIN',
    'MEMBER'
  ];

  // Household form
  readonly householdForm = this.fb.nonNullable.group({
    name: ['', [
      Validators.required,
      Validators.maxLength(120)
    ]],
    description: ['', Validators.maxLength(500)],
    currency: ['INR', [
      Validators.required,
      Validators.pattern(/^[A-Z]{3}$/)
    ]]
  });

  // Member form
  readonly memberForm = this.fb.nonNullable.group({
    fullName: ['', [
      Validators.required,
      Validators.maxLength(120)
    ]],
    relationship: ['OTHER' as Relationship, Validators.required],
    role: ['MEMBER' as MemberRole, Validators.required],
    dateOfBirth: [''],
    email: ['', Validators.email],
    phone: [''],
    identityUserId: ['']
  });

  constructor() {
    this.initializeHousehold();
  }

  // ---------------------------------------
  // CUSTOMER-SPECIFIC STORAGE KEY
  // ---------------------------------------

  private getStorageKey(): string {

    const user = this.auth.currentUser() as {
      email?: string;
      userId?: string;
      id?: string;
    } | null;

    const identity =
      user?.email ||
      user?.userId ||
      user?.id;

    return `finlongevity_household_created_${identity || 'unknown'}`;
  }

  // ---------------------------------------
  // INITIALIZE HOUSEHOLD
  // ---------------------------------------

  initializeHousehold(): void {

    const created = localStorage.getItem(this.getStorageKey());

    if (created === 'true') {

      // Returning customer.
      this.load();

    } else {

      // Fresh customer: no GET calls.
      this.household.set(null);
      this.members.set([]);

      this.loading.set(false);
      this.showWelcome.set(true);
      this.showHouseholdForm.set(false);
    }
  }

  // ---------------------------------------
  // LOAD EXISTING HOUSEHOLD
  // ---------------------------------------

  load(): void {

    this.loading.set(true);
    this.error.set('');

    this.api.getHousehold<Household>().subscribe({

      next: value => {

        this.household.set(value);

        this.householdForm.patchValue({
          name: value.name || '',
          description: value.description || '',
          currency: value.currency || 'INR'
        });

        this.showWelcome.set(false);
        this.showHouseholdForm.set(false);

        // Only load members when household exists.
        this.loadMembers();
      },

      error: err => {

        this.loading.set(false);

        if (err?.status === 404) {

          localStorage.removeItem(this.getStorageKey());

          this.household.set(null);
          this.members.set([]);

          this.showWelcome.set(true);
          this.showHouseholdForm.set(false);

        } else {

          this.error.set(
            err?.error?.message ||
            'Could not load household details.'
          );
        }
      }
    });
  }

  // ---------------------------------------
  // LOAD HOUSEHOLD MEMBERS
  // ---------------------------------------

  private loadMembers(): void {

    this.api.getHouseholdMembers<unknown>().subscribe({

      next: result => {

        this.members.set(
          this.toArray<HouseholdMember>(result)
        );

        this.loading.set(false);
      },

      error: err => {

        if (err?.status === 404) {

          this.members.set([]);

        } else {

          this.error.set(
            err?.error?.message ||
            'Could not load household members.'
          );
        }

        this.loading.set(false);
      }
    });
  }

  // Handle different API response formats.
  private toArray<T>(result: unknown): T[] {

    if (Array.isArray(result)) {
      return result as T[];
    }

    if (result && typeof result === 'object') {

      const value = result as Record<string, unknown>;

      for (const key of [
        'content',
        'items',
        'members',
        'data'
      ]) {

        if (Array.isArray(value[key])) {
          return value[key] as T[];
        }
      }
    }

    return [];
  }

  // ---------------------------------------
  // CREATE HOUSEHOLD POPUP
  // ---------------------------------------

  startHouseholdCreation(): void {

    if (this.household()) return;

    this.householdForm.reset({
      name: '',
      description: '',
      currency: 'INR'
    });

    this.message.set('');
    this.error.set('');

    this.showWelcome.set(false);
    this.showHouseholdForm.set(true);
  }

  // ---------------------------------------
  // EDIT HOUSEHOLD
  // ---------------------------------------

  editHousehold(): void {

    const saved = this.household();

    if (!saved) return;

    this.householdForm.patchValue({
      name: saved.name || '',
      description: saved.description || '',
      currency: saved.currency || 'INR'
    });

    this.message.set('');
    this.error.set('');

    this.showWelcome.set(false);
    this.showHouseholdForm.set(true);
  }

  cancelHouseholdForm(): void {

    this.showHouseholdForm.set(false);
    this.error.set('');

    if (this.household()) {
      this.showWelcome.set(false);
    } else {
      this.showWelcome.set(true);
    }
  }

  // ---------------------------------------
  // CREATE / UPDATE HOUSEHOLD
  // ---------------------------------------

  saveHousehold(): void {

    if (this.householdForm.invalid || this.saving()) {

      this.householdForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.error.set('');
    this.message.set('');

    const request = this.householdForm.getRawValue();

    const isNew = this.household() === null;

    const operation = isNew
      ? this.api.createHousehold<Household>(request)
      : this.api.saveHousehold<Household>(request);

    operation.subscribe({

      next: saved => {

        // Mark household as created for this customer.
        localStorage.setItem(this.getStorageKey(), 'true');

        this.household.set(saved ?? request);

        this.saving.set(false);

        this.showWelcome.set(false);
        this.showHouseholdForm.set(false);

        this.message.set(
          isNew
            ? 'Household created successfully.'
            : 'Household updated successfully.'
        );

        // GET household and members only after successful save.
        this.load();
      },

      error: err => {

        this.saving.set(false);

        this.error.set(
          err?.error?.message ||
          'Could not save household details.'
        );
      }
    });
  }

  // ---------------------------------------
  // ADD HOUSEHOLD MEMBER
  // ---------------------------------------

  openAddMember(): void {

    if (!this.household()) return;

    this.editingMemberId.set('');

    this.memberForm.reset({
      fullName: '',
      relationship: 'OTHER',
      role: 'MEMBER',
      dateOfBirth: '',
      email: '',
      phone: '',
      identityUserId: ''
    });

    this.error.set('');
    this.showMemberForm.set(true);
  }

  // ---------------------------------------
  // EDIT HOUSEHOLD MEMBER
  // ---------------------------------------

  editMember(member: HouseholdMember): void {

    this.editingMemberId.set(member.id);

    this.memberForm.reset({
      fullName: member.fullName,
      relationship: member.relationship,
      role: member.role,
      dateOfBirth: member.dateOfBirth || '',
      email: member.email || '',
      phone: member.phone || '',
      identityUserId: member.identityUserId || ''
    });

    this.error.set('');
    this.showMemberForm.set(true);
  }

  closeMemberForm(): void {

    this.showMemberForm.set(false);
    this.editingMemberId.set('');
  }

  // ---------------------------------------
  // SAVE HOUSEHOLD MEMBER
  // ---------------------------------------

  saveMember(): void {

    if (this.memberForm.invalid || this.saving()) {

      this.memberForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.error.set('');

    const raw = this.memberForm.getRawValue();

    const request = Object.fromEntries(
      Object.entries(raw).filter(
        ([, value]) => value !== ''
      )
    );

    const id = this.editingMemberId();

    const operation = id
      ? this.api.updateHouseholdMember<HouseholdMember>(
        id,
        request
      )
      : this.api.addHouseholdMember<HouseholdMember>(
        request
      );

    operation.subscribe({

      next: () => {

        this.saving.set(false);
        this.closeMemberForm();

        this.message.set(
          id
            ? 'Household member updated successfully.'
            : 'Household member added successfully.'
        );

        this.loadMembers();
      },

      error: err => {

        this.saving.set(false);

        this.error.set(
          err?.error?.message ||
          'Could not save household member.'
        );
      }
    });
  }

  // ---------------------------------------
  // DELETE HOUSEHOLD MEMBER
  // ---------------------------------------

  deleteMember(member: HouseholdMember): void {

    if (
      !globalThis.confirm(
        `Remove ${member.fullName} from this household?`
      )
    ) {
      return;
    }

    this.deletingId.set(member.id);
    this.error.set('');

    this.api.deleteHouseholdMember(member.id).subscribe({

      next: () => {

        this.deletingId.set('');

        this.members.update(items =>
          items.filter(item => item.id !== member.id)
        );

        this.message.set('Household member removed.');
      },

      error: err => {

        this.deletingId.set('');

        this.error.set(
          err?.error?.message ||
          'Could not remove household member.'
        );
      }
    });
  }

  // ---------------------------------------
  // HELPERS
  // ---------------------------------------

  relationshipLabel(value: Relationship): string {

    return this.relationships.find(
      item => item.value === value
    )?.label ?? value;
  }

  trackMember(_: number, member: HouseholdMember): string {
    return member.id;
  }
}
