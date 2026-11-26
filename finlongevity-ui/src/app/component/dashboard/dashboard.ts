import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CustomerService } from '../../services/customer-service';
import { AuthService } from '../../services/auth-service';


interface NetWorth {
  currency: string;
  totalAssets: number;
  totalLiabilities: number;
  netWorth: number;
  calculatedAt: string;
}

@Component({
  imports: [DatePipe, DecimalPipe, RouterLink],
  selector: 'app-dashboard',
  styleUrl: './dashboard.css',
  templateUrl: './dashboard.html',
})
export class Dashboard {
  private readonly api = inject(CustomerService);
  private readonly auth = inject(AuthService);
  readonly netWorth = signal<NetWorth | null>(null);
  readonly error = signal('');
  get firstName(): string { return this.auth.currentUser()?.fullName?.split(' ')[0] || 'there'; }
  constructor() { this.load(); }
  load(): void {
    this.error.set('');
    this.api.getNetWorth().subscribe({
      next: data => this.netWorth.set(data as NetWorth),
      error: err => this.error.set(err?.error?.message || 'Could not load net worth. Add your financial profile and assets first, or check that the Customer Service is running.')
    });
  }
  money(value?: number): string {
    if (value == null) return '—';
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: this.netWorth()?.currency || 'INR', maximumFractionDigits: 0 }).format(value);
  }
}
