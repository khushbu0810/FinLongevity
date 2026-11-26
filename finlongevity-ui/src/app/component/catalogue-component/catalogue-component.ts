import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CatalogueService } from '../../services/catalogue-service';
import { Router } from '@angular/router';
import { InvestmentDraftService, InvestmentProductType } from '../../services/investment-draft-service';

type ProductType = 'FIXED_DEPOSIT' | 'MUTUAL_FUND' | 'STOCK' | 'BOND' | 'INSURANCE' | 'OTHER';
interface Product { id: string; providerId: string; providerName?: string; externalProductCode: string; name: string; description?: string; productType: ProductType; status: 'DRAFT' | 'PENDING_REVIEW' | 'ACTIVE' | 'INACTIVE' | 'ARCHIVED'; currency: string; minimumInvestment: number; maximumInvestment?: number | null; featured: boolean; createdAt?: string; updatedAt?: string; }
@Component({
  standalone: true, imports: [CurrencyPipe, DatePipe, FormsModule],
  selector: 'app-catalogue-component',
  styleUrl: './catalogue-component.css',
  templateUrl: './catalogue-component.html',
})
export class CatalogueComponent {
  private readonly api = inject(CatalogueService);
  private readonly router = inject(Router);
  private readonly investmentDraft = inject(InvestmentDraftService);
  readonly types: ProductType[] = ['FIXED_DEPOSIT', 'MUTUAL_FUND', 'STOCK', 'BOND', 'INSURANCE', 'OTHER'];
  readonly products = signal<Product[]>([]);
  readonly selectedProduct = signal<Product | null>(null);
  readonly loading = signal(false); readonly detailLoading = signal(false);
  readonly error = signal(''); readonly query = signal(''); readonly selectedType = signal('');
  readonly filteredProducts = computed(() => {
    const q = this.query().trim().toLowerCase(); const type = this.selectedType();
    return this.products().filter(p => (!type || p.productType === type) && (!q || [p.name, p.providerName, p.description, p.externalProductCode].some(v => (v || '').toLowerCase().includes(q))));
  });
  constructor() { this.loadCatalogue(); }
  loadCatalogue(): void { this.loading.set(true); this.error.set(''); this.api.getCatalogue<Product[]>(this.selectedType()).subscribe({ next: rows => { this.products.set(rows ?? []); this.loading.set(false); }, error: e => { this.loading.set(false); this.error.set(e?.error?.message || 'Could not load the product catalogue. Check the Provider Product Service and API Gateway.'); } }); }
  changeType(value: string): void { this.selectedType.set(value); this.loadCatalogue(); }
  clearFilters(): void { this.query.set(''); this.selectedType.set(''); this.loadCatalogue(); }
  openDetails(product: Product): void { this.selectedProduct.set(product); this.detailLoading.set(true); this.error.set(''); this.api.getProduct<Product>(product.id).subscribe({ next: p => { this.selectedProduct.set(p); this.detailLoading.set(false); }, error: e => { this.detailLoading.set(false); this.error.set(e?.error?.message || 'Could not load product details.'); } }); }
  closeDetails(): void { this.selectedProduct.set(null); }
  canStartInvestment(product: Product): boolean { return product.status === 'ACTIVE' && product.productType !== 'INSURANCE'; }
  startInvestment(product: Product): void {
    if (!this.canStartInvestment(product)) return;
    this.investmentDraft.selectProduct({ id: product.id, name: product.name, productType: product.productType as InvestmentProductType, currency: product.currency, minimumInvestment: Number(product.minimumInvestment || 0), maximumInvestment: product.maximumInvestment });
    this.router.navigate(['/investments']);
  }
  formatLabel(value: string | null | undefined): string { return (value || '').toLowerCase().split('_').map(x => x.charAt(0).toUpperCase() + x.slice(1)).join(' '); }
  statusClass(status: string): string { return status === 'ACTIVE' ? 'status-positive' : 'status-neutral'; }
}
