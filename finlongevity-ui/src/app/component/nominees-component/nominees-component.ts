import { Component, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer-service';

type NomineeRelationship = 'SPOUSE' | 'SON' | 'DAUGHTER' | 'FATHER' | 'MOTHER' | 'BROTHER' | 'SISTER' | 'GRANDFATHER' | 'GRANDMOTHER' | 'GRANDSON' | 'GRANDDAUGHTER' | 'LEGAL_GUARDIAN' | 'OTHER';
interface Nominee { id?: string; nomineeId?: string; fullName: string; relationship: NomineeRelationship; dateOfBirth: string; allocationPercentage: number; email?: string; phone?: string; guardianName?: string; notes?: string; }

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, DecimalPipe],
  selector: 'app-nominees-component',
  styleUrl: './nominees-component.css',
  templateUrl: './nominees-component.html',
})
export class NomineesComponent {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(CustomerService);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly deletingId = signal('');
  readonly error = signal('');
  readonly message = signal('');
  readonly nominees = signal<Nominee[]>([]);
  readonly showForm = signal(false);
  readonly editingId = signal('');

  readonly relationships: { value: NomineeRelationship; label: string }[] = [
    { value: 'SPOUSE', label: 'Spouse' }, { value: 'SON', label: 'Son' }, { value: 'DAUGHTER', label: 'Daughter' },
    { value: 'FATHER', label: 'Father' }, { value: 'MOTHER', label: 'Mother' }, { value: 'BROTHER', label: 'Brother' },
    { value: 'SISTER', label: 'Sister' }, { value: 'GRANDFATHER', label: 'Grandfather' }, { value: 'GRANDMOTHER', label: 'Grandmother' },
    { value: 'GRANDSON', label: 'Grandson' }, { value: 'GRANDDAUGHTER', label: 'Granddaughter' },
    { value: 'LEGAL_GUARDIAN', label: 'Legal guardian' }, { value: 'OTHER', label: 'Other' }
  ];

  readonly form = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.maxLength(120)]],
    relationship: ['OTHER' as NomineeRelationship, Validators.required],
    dateOfBirth: ['', Validators.required],
    allocationPercentage: [0, [Validators.required, Validators.min(0.01), Validators.max(100)]],
    email: ['', Validators.email], phone: [''], guardianName: [''], notes: ['', Validators.maxLength(1000)]
  });

  constructor() { this.load(); }

  load(): void {
    this.loading.set(true); this.error.set('');
    this.api.getNominees<unknown>().subscribe({
      next: result => { this.nominees.set(this.toArray<Nominee>(result)); this.loading.set(false); },
      error: err => {
        if (err?.status === 404) this.nominees.set([]);
        else this.error.set(err?.error?.message || 'Could not load nominees.');
        this.loading.set(false);
      }
    });
  }

  private toArray<T>(result: unknown): T[] {
    if (Array.isArray(result)) return result as T[];
    if (result && typeof result === 'object') {
      const value = result as Record<string, unknown>;
      for (const key of ['content', 'items', 'nominees', 'data']) if (Array.isArray(value[key])) return value[key] as T[];
    }
    return [];
  }

  get totalAllocation(): number {
    return this.nominees().reduce((sum, nominee) => sum + Number(nominee.allocationPercentage || 0), 0);
  }

  openAdd(): void {
    this.editingId.set('');
    this.form.reset({ fullName: '', relationship: 'OTHER', dateOfBirth: '', allocationPercentage: 0, email: '', phone: '', guardianName: '', notes: '' });
    this.showForm.set(true); this.error.set('');
  }

  edit(nominee: Nominee): void {
    const id = nominee.id ?? nominee.nomineeId;
    if (!id) { this.error.set('This nominee record has no identifier and cannot be edited.'); return; }
    this.editingId.set(id);
    this.form.reset({
      fullName: nominee.fullName, relationship: nominee.relationship, dateOfBirth: nominee.dateOfBirth,
      allocationPercentage: Number(nominee.allocationPercentage), email: nominee.email ?? '', phone: nominee.phone ?? '',
      guardianName: nominee.guardianName ?? '', notes: nominee.notes ?? ''
    });
    this.showForm.set(true); this.error.set('');
  }

  closeForm(): void { this.showForm.set(false); this.editingId.set(''); }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true); this.error.set(''); this.message.set('');
    const raw = this.form.getRawValue();
    const request = Object.fromEntries(Object.entries(raw).filter(([, value]) => value !== ''));
    const id = this.editingId();
    const action = id ? this.api.updateNominee<Nominee>(id, request) : this.api.createNominee<Nominee>(request);
    action.subscribe({
      next: () => { this.saving.set(false); this.closeForm(); this.message.set(id ? 'Nominee updated successfully.' : 'Nominee added successfully.'); this.load(); },
      error: err => { this.saving.set(false); this.error.set(err?.error?.message || 'Could not save nominee details.'); }
    });
  }

  remove(nominee: Nominee): void {
    const id = nominee.id ?? nominee.nomineeId;
    if (!id) { this.error.set('This nominee record has no identifier and cannot be removed.'); return; }
    if (!globalThis.confirm(`Remove ${nominee.fullName} from your nominees?`)) return;
    this.deletingId.set(id); this.error.set('');
    this.api.deleteNominee(id).subscribe({
      next: () => { this.deletingId.set(''); this.nominees.update(items => items.filter(item => (item.id ?? item.nomineeId) !== id)); this.message.set('Nominee removed.'); },
      error: err => { this.deletingId.set(''); this.error.set(err?.error?.message || 'Could not remove nominee.'); }
    });
  }

  relationshipLabel(value: NomineeRelationship): string { return this.relationships.find(item => item.value === value)?.label ?? value; }
  trackNominee(_: number, nominee: Nominee): string { return nominee.id ?? nominee.nomineeId ?? nominee.fullName; }
}
