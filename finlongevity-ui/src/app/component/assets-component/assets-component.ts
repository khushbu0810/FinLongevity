import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer-service';

type AssetCategory =
  | 'BANK_ACCOUNT' | 'FIXED_DEPOSIT' | 'MUTUAL_FUND' | 'STOCK' | 'BOND'
  | 'REAL_ESTATE' | 'VEHICLE' | 'PHYSICAL_GOLD' | 'DIGITAL_GOLD'
  | 'EPF' | 'PPF' | 'NPS' | 'OTHER';

interface Asset {
  id: string;
  name: string;
  category: AssetCategory;
  currentValue: number;
  currency: string;
  valuationDate: string;
  liquid: boolean;
  notes?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

interface AssetRequest {
  name: string;
  category: AssetCategory;
  currentValue: number;
  currency: string;
  valuationDate: string;
  liquid: boolean;
  notes: string;
}

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe, DatePipe],
  selector: 'app-assets-component',
  styleUrl: './assets-component.css',
  templateUrl: './assets-component.html',
})
export class AssetsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly customerApi = inject(CustomerService);

  readonly categories: AssetCategory[] = [
    'BANK_ACCOUNT', 'FIXED_DEPOSIT', 'MUTUAL_FUND', 'STOCK', 'BOND',
    'REAL_ESTATE', 'VEHICLE', 'PHYSICAL_GOLD', 'DIGITAL_GOLD', 'EPF', 'PPF', 'NPS', 'OTHER'
  ];

  readonly assets = signal<Asset[]>([]);
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
    category: ['' as AssetCategory | '', Validators.required],
    currentValue: [0, [Validators.required, Validators.min(0)]],
    currency: ['INR', [Validators.required, Validators.pattern(/^[A-Z]{3}$/)]],
    valuationDate: [new Date().toISOString().slice(0, 10), Validators.required],
    liquid: [true],
    notes: ['', Validators.maxLength(500)]
  });

  constructor() {
    this.loadAssets();
  }

  loadAssets(): void {
    this.loading.set(true);
    this.error.set('');
    this.customerApi.getAssets<Asset[]>().subscribe({
      next: (items) => {
        this.assets.set(items ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Could not load your assets. Check the Customer Service and API Gateway.');
        this.loading.set(false);
      }
    });
  }

  get filteredAssets(): Asset[] {
    const category = this.selectedCategory();
    return category === 'ALL' ? this.assets() : this.assets().filter(asset => asset.category === category);
  }

  get totalInrAssets(): number {
    return this.assets()
      .filter(asset => asset.currency === 'INR')
      .reduce((total, asset) => total + Number(asset.currentValue || 0), 0);
  }

  get liquidInrAssets(): number {
    return this.assets()
      .filter(asset => asset.currency === 'INR' && asset.liquid)
      .reduce((total, asset) => total + Number(asset.currentValue || 0), 0);
  }

  get currencyCount(): number {
    return new Set(this.assets().map(asset => asset.currency)).size;
  }

  formatCategory(category: string): string {
    return category.toLowerCase().replaceAll('_', ' ').replace(/\b\w/g, letter => letter.toUpperCase());
  }

  openCreateForm(): void {
    this.editingId.set(null);
    this.form.reset({
      name: '', category: '', currentValue: 0, currency: 'INR',
      valuationDate: new Date().toISOString().slice(0, 10), liquid: true, notes: ''
    });
    this.error.set('');
    this.message.set('');
    this.showForm.set(true);
  }

  openEditForm(asset: Asset): void {
    this.editingId.set(asset.id);
    this.form.reset({
      name: asset.name,
      category: asset.category,
      currentValue: asset.currentValue,
      currency: asset.currency,
      valuationDate: asset.valuationDate,
      liquid: asset.liquid,
      notes: asset.notes ?? ''
    });
    this.error.set('');
    this.message.set('');
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.editingId.set(null);
  }

  saveAsset(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const request: AssetRequest = {
      name: value.name.trim(),
      category: value.category as AssetCategory,
      currentValue: Number(value.currentValue),
      currency: value.currency.toUpperCase(),
      valuationDate: value.valuationDate,
      liquid: value.liquid,
      notes: value.notes.trim()
    };

    this.saving.set(true);
    this.error.set('');
    const id = this.editingId();
    const operation = id
      ? this.customerApi.updateAsset<Asset>(id, request)
      : this.customerApi.createAsset<Asset>(request);

    operation.subscribe({
      next: () => {
        this.saving.set(false);
        this.closeForm();
        this.message.set(id ? 'Asset updated successfully.' : 'Asset added successfully.');
        this.loadAssets();
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(err?.error?.message || 'Unable to save the asset. Please verify the entered values.');
      }
    });
  }

  deleteAsset(asset: Asset): void {
    if (!confirm(`Delete “${asset.name}” from your assets?`)) return;
    this.deletingId.set(asset.id);
    this.error.set('');
    this.customerApi.deleteAsset(asset.id).subscribe({
      next: () => {
        this.deletingId.set(null);
        this.message.set('Asset deleted successfully.');
        this.loadAssets();
      },
      error: (err) => {
        this.deletingId.set(null);
        this.error.set(err?.error?.message || 'Unable to delete this asset.');
      }
    });
  }
}

