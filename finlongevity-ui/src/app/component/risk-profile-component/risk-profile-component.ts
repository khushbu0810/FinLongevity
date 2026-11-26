import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer-service';

type RiskTolerance = 'CONSERVATIVE' | 'MODERATE' | 'AGGRESSIVE';
type InvestmentExperience = 'BEGINNER' | 'INTERMEDIATE' | 'EXPERIENCED';
type InvestmentHorizon = 'SHORT_TERM' | 'MEDIUM_TERM' | 'LONG_TERM';
type LiquidityRequirement = 'HIGH' | 'MEDIUM' | 'LOW';
type InvestmentPreference = 'FIXED_DEPOSIT' | 'MUTUAL_FUND' | 'STOCKS' | 'BONDS' | 'GOLD';
type FinancialObjective =
  | 'WEALTH_CREATION' | 'RETIREMENT_PLANNING' | 'CAPITAL_PRESERVATION'
  | 'REGULAR_INCOME' | 'CHILD_EDUCATION' | 'HOME_PURCHASE'
  | 'TAX_SAVING' | 'EMERGENCY_FUND';

interface RiskProfile {
  id?: string;
  riskTolerance: RiskTolerance;
  investmentExperience: InvestmentExperience;
  investmentHorizon: InvestmentHorizon;
  liquidityRequirement: LiquidityRequirement;
  investmentPreferences: InvestmentPreference[];
  financialObjectives: FinancialObjective[];
  createdAt?: string;
  updatedAt?: string;
}

@Component({
  standalone: true,
  imports: [ReactiveFormsModule],
  selector: 'app-risk-profile-component',
  styleUrl: './risk-profile-component.css',
  templateUrl: './risk-profile-component.html',
})
export class RiskProfileComponent {
  private readonly fb = inject(FormBuilder);
  private readonly customerApi = inject(CustomerService);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly message = signal('');
  readonly hasProfile = signal(false);

  readonly preferenceOptions: { value: InvestmentPreference; label: string; description: string }[] = [
    { value: 'FIXED_DEPOSIT', label: 'Fixed deposits', description: 'Predictable returns and capital stability' },
    { value: 'MUTUAL_FUND', label: 'Mutual funds', description: 'Professionally managed diversified investments' },
    { value: 'STOCKS', label: 'Stocks', description: 'Direct equity market exposure' },
    { value: 'BONDS', label: 'Bonds', description: 'Fixed-income investment instruments' },
    { value: 'GOLD', label: 'Gold', description: 'Physical or market-linked gold exposure' }
  ];

  readonly objectiveOptions: { value: FinancialObjective; label: string; description: string }[] = [
    { value: 'WEALTH_CREATION', label: 'Wealth creation', description: 'Grow long-term net worth' },
    { value: 'RETIREMENT_PLANNING', label: 'Retirement planning', description: 'Build a retirement corpus' },
    { value: 'CAPITAL_PRESERVATION', label: 'Capital preservation', description: 'Prioritise protecting existing capital' },
    { value: 'REGULAR_INCOME', label: 'Regular income', description: 'Generate a steady income stream' },
    { value: 'CHILD_EDUCATION', label: 'Child education', description: 'Plan for future education costs' },
    { value: 'HOME_PURCHASE', label: 'Home purchase', description: 'Save towards buying a home' },
    { value: 'TAX_SAVING', label: 'Tax saving', description: 'Include tax-efficient investments' },
    { value: 'EMERGENCY_FUND', label: 'Emergency fund', description: 'Build accessible financial reserves' }
  ];

  readonly selectedPreferences = signal<InvestmentPreference[]>([]);
  readonly selectedObjectives = signal<FinancialObjective[]>([]);

  readonly form = this.fb.nonNullable.group({
    riskTolerance: ['MODERATE' as RiskTolerance, Validators.required],
    investmentExperience: ['BEGINNER' as InvestmentExperience, Validators.required],
    investmentHorizon: ['LONG_TERM' as InvestmentHorizon, Validators.required],
    liquidityRequirement: ['MEDIUM' as LiquidityRequirement, Validators.required]
  });

  constructor() {
    this.loadProfile();
  }

  loadProfile(): void {
    this.loading.set(true);
    this.error.set('');
    this.customerApi.getRiskProfile<RiskProfile>().subscribe({
      next: profile => {
        this.form.patchValue({
          riskTolerance: profile.riskTolerance,
          investmentExperience: profile.investmentExperience,
          investmentHorizon: profile.investmentHorizon,
          liquidityRequirement: profile.liquidityRequirement
        });
        this.selectedPreferences.set(profile.investmentPreferences ?? []);
        this.selectedObjectives.set(profile.financialObjectives ?? []);
        this.hasProfile.set(true);
        this.loading.set(false);
      },
      error: err => {
        // A missing risk profile is a normal first-time state; other API errors are shown.
        if (err?.status === 404) {
          this.hasProfile.set(false);
        } else {
          this.error.set(err?.error?.message || 'Could not load your risk profile. Check the Customer Service and API Gateway.');
        }
        this.loading.set(false);
      }
    });
  }

  togglePreference(value: InvestmentPreference, checked: boolean): void {
    this.selectedPreferences.update(current =>
      checked ? [...current, value] : current.filter(item => item !== value)
    );
  }

  toggleObjective(value: FinancialObjective, checked: boolean): void {
    this.selectedObjectives.update(current =>
      checked ? [...current, value] : current.filter(item => item !== value)
    );
  }

  isPreferenceSelected(value: InvestmentPreference): boolean {
    return this.selectedPreferences().includes(value);
  }

  isObjectiveSelected(value: FinancialObjective): boolean {
    return this.selectedObjectives().includes(value);
  }

  saveProfile(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (!this.selectedPreferences().length || !this.selectedObjectives().length) {
      this.error.set('Select at least one investment preference and one financial objective.');
      return;
    }

    const request: RiskProfile = {
      ...this.form.getRawValue(),
      investmentPreferences: this.selectedPreferences(),
      financialObjectives: this.selectedObjectives()
    };

    this.saving.set(true);
    this.error.set('');
    this.message.set('');
    this.customerApi.saveRiskProfile<RiskProfile>(request).subscribe({
      next: response => {
        this.saving.set(false);
        this.hasProfile.set(true);
        this.selectedPreferences.set(response.investmentPreferences ?? request.investmentPreferences);
        this.selectedObjectives.set(response.financialObjectives ?? request.financialObjectives);
        this.message.set('Your risk profile has been saved successfully.');
      },
      error: err => {
        this.saving.set(false);
        this.error.set(err?.error?.message || 'Unable to save your risk profile. Please check your selections.');
      }
    });
  }

  resetForm(): void {
    this.form.reset({
      riskTolerance: 'MODERATE',
      investmentExperience: 'BEGINNER',
      investmentHorizon: 'LONG_TERM',
      liquidityRequirement: 'MEDIUM'
    });
    this.selectedPreferences.set([]);
    this.selectedObjectives.set([]);
    this.message.set('');
    this.error.set('');
  }
}
