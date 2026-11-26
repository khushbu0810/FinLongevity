import { Injectable, signal } from '@angular/core';

export type InvestmentProductType = 'FIXED_DEPOSIT' | 'MUTUAL_FUND' | 'STOCK' | 'BOND' | 'OTHER';
export interface SelectedCatalogueProduct {
    id: string;
    name: string;
    productType: InvestmentProductType;
    currency: string;
    minimumInvestment: number;
    maximumInvestment?: number | null;
}

@Injectable({ providedIn: 'root' })
export class InvestmentDraftService {
    private readonly pendingProduct = signal<SelectedCatalogueProduct | null>(null);
    selectProduct(product: SelectedCatalogueProduct): void { this.pendingProduct.set(product); }
    consumeSelectedProduct(): SelectedCatalogueProduct | null {
        const product = this.pendingProduct();
        this.pendingProduct.set(null);
        return product;
    }
}
