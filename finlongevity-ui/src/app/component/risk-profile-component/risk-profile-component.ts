
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer-service';
import { AuthService } from '../../services/auth-service';

type RiskTolerance = 'CONSERVATIVE' | 'MODERATE' | 'AGGRESSIVE';
type InvestmentExperience = 'BEGINNER' | 'INTERMEDIATE' | 'EXPERIENCED';
type InvestmentHorizon = 'SHORT_TERM' | 'MEDIUM_TERM' | 'LONG_TERM';
type LiquidityRequirement = 'HIGH' | 'MEDIUM' | 'LOW';

type InvestmentPreference =
  | 'FIXED_DEPOSIT'
  | 'MUTUAL_FUND'
  | 'STOCKS'
  | 'BONDS'
  | 'GOLD';

type FinancialObjective =
  | 'WEALTH_CREATION'
  | 'RETIREMENT_PLANNING'
  | 'CAPITAL_PRESERVATION'
  | 'REGULAR_INCOME'
  | 'CHILD_EDUCATION'
  | 'HOME_PURCHASE'
  | 'TAX_SAVING'
  | 'EMERGENCY_FUND';

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
  private readonly auth = inject(AuthService);

  // ---------------------------------------
  // STATE
  // ---------------------------------------

  readonly profile = signal<RiskProfile | null>(null);

  readonly loading = signal(false);
  readonly saving = signal(false);

  readonly showWelcome = signal(false);
  readonly showForm = signal(false);

  readonly error = signal('');
  readonly message = signal('');

  readonly hasProfile = signal(false);

  readonly selectedPreferences =
    signal<InvestmentPreference[]>([]);

  readonly selectedObjectives =
    signal<FinancialObjective[]>([]);

  // ---------------------------------------
  // OPTIONS
  // ---------------------------------------

  readonly preferenceOptions: {
    value: InvestmentPreference;
    label: string;
    description: string;
  }[] = [

      {
        value: 'FIXED_DEPOSIT',
        label: 'Fixed deposits',
        description: 'Predictable returns and capital stability'
      },
      {
        value: 'MUTUAL_FUND',
        label: 'Mutual funds',
        description: 'Professionally managed diversified investments'
      },
      {
        value: 'STOCKS',
        label: 'Stocks',
        description: 'Direct equity market exposure'
      },
      {
        value: 'BONDS',
        label: 'Bonds',
        description: 'Fixed-income investment instruments'
      },
      {
        value: 'GOLD',
        label: 'Gold',
        description: 'Physical or market-linked gold exposure'
      }
    ];

  readonly objectiveOptions: {
    value: FinancialObjective;
    label: string;
    description: string;
  }[] = [

      {
        value: 'WEALTH_CREATION',
        label: 'Wealth creation',
        description: 'Grow long-term net worth'
      },
      {
        value: 'RETIREMENT_PLANNING',
        label: 'Retirement planning',
        description: 'Build a retirement corpus'
      },
      {
        value: 'CAPITAL_PRESERVATION',
        label: 'Capital preservation',
        description: 'Prioritise protecting existing capital'
      },
      {
        value: 'REGULAR_INCOME',
        label: 'Regular income',
        description: 'Generate a steady income stream'
      },
      {
        value: 'CHILD_EDUCATION',
        label: 'Child education',
        description: 'Plan for future education costs'
      },
      {
        value: 'HOME_PURCHASE',
        label: 'Home purchase',
        description: 'Save towards buying a home'
      },
      {
        value: 'TAX_SAVING',
        label: 'Tax saving',
        description: 'Include tax-efficient investments'
      },
      {
        value: 'EMERGENCY_FUND',
        label: 'Emergency fund',
        description: 'Build accessible financial reserves'
      }
    ];

  // ---------------------------------------
  // FORM
  // ---------------------------------------

  readonly form = this.fb.nonNullable.group({

    riskTolerance: [
      'MODERATE' as RiskTolerance,
      Validators.required
    ],

    investmentExperience: [
      'BEGINNER' as InvestmentExperience,
      Validators.required
    ],

    investmentHorizon: [
      'LONG_TERM' as InvestmentHorizon,
      Validators.required
    ],

    liquidityRequirement: [
      'MEDIUM' as LiquidityRequirement,
      Validators.required
    ]

  });

  constructor() {
    this.initializeRiskProfile();
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

    return `finlongevity_risk_profile_created_${identity || 'unknown'}`;
  }

  // ---------------------------------------
  // INITIALIZE
  // ---------------------------------------

  initializeRiskProfile(): void {

    const created = localStorage.getItem(
      this.getStorageKey()
    );

    if (created === 'true') {

      // Returning customer.
      this.loadProfile();

    } else {

      // Fresh customer.
      // Do not call GET API here.

      this.profile.set(null);
      this.hasProfile.set(false);

      this.selectedPreferences.set([]);
      this.selectedObjectives.set([]);

      this.loading.set(false);

      this.showWelcome.set(true);
      this.showForm.set(false);
    }
  }

  // ---------------------------------------
  // LOAD EXISTING PROFILE
  // ---------------------------------------

  loadProfile(): void {

    this.loading.set(true);
    this.error.set('');
    this.message.set('');

    this.customerApi.getRiskProfile<RiskProfile>()
      .subscribe({

        next: savedProfile => {

          this.profile.set(savedProfile);
          this.hasProfile.set(true);

          this.form.patchValue({

            riskTolerance: savedProfile.riskTolerance,
            investmentExperience: savedProfile.investmentExperience,
            investmentHorizon: savedProfile.investmentHorizon,
            liquidityRequirement: savedProfile.liquidityRequirement

          });

          this.selectedPreferences.set(
            savedProfile.investmentPreferences ?? []
          );

          this.selectedObjectives.set(
            savedProfile.financialObjectives ?? []
          );

          // Remember that this customer has a profile.
          localStorage.setItem(
            this.getStorageKey(),
            'true'
          );

          this.showWelcome.set(false);
          this.showForm.set(false);

          this.loading.set(false);
        },

        error: err => {

          this.loading.set(false);

          if (err?.status === 404) {

            this.profile.set(null);
            this.hasProfile.set(false);

            localStorage.removeItem(
              this.getStorageKey()
            );

            this.showWelcome.set(true);
            this.showForm.set(false);

          } else {

            this.error.set(
              err?.error?.message ||
              'Could not load your risk profile. Please try again.'
            );

            this.showWelcome.set(false);
            this.showForm.set(false);
          }
        }
      });
  }

  // ---------------------------------------
  // CREATE RISK PROFILE
  // ---------------------------------------

  startProfileCreation(): void {

    if (this.hasProfile()) {
      return;
    }

    this.resetForm();

    this.error.set('');
    this.message.set('');

    this.showWelcome.set(false);
    this.showForm.set(true);
  }

  // ---------------------------------------
  // EDIT EXISTING PROFILE
  // ---------------------------------------

  editProfile(): void {

    const saved = this.profile();

    if (!saved) {
      return;
    }

    this.form.patchValue({

      riskTolerance: saved.riskTolerance,
      investmentExperience: saved.investmentExperience,
      investmentHorizon: saved.investmentHorizon,
      liquidityRequirement: saved.liquidityRequirement

    });

    this.selectedPreferences.set(
      saved.investmentPreferences ?? []
    );

    this.selectedObjectives.set(
      saved.financialObjectives ?? []
    );

    this.error.set('');
    this.message.set('');

    this.showWelcome.set(false);
    this.showForm.set(true);
  }

  // ---------------------------------------
  // CANCEL FORM
  // ---------------------------------------

  cancelForm(): void {

    this.showForm.set(false);
    this.error.set('');

    if (this.hasProfile()) {

      this.showWelcome.set(false);

    } else {

      this.showWelcome.set(true);
    }
  }

  // ---------------------------------------
  // PREFERENCE SELECTION
  // ---------------------------------------

  togglePreference(
    value: InvestmentPreference,
    checked: boolean
  ): void {

    this.selectedPreferences.update(current => {

      if (checked) {

        return current.includes(value)
          ? current
          : [...current, value];

      }

      return current.filter(item => item !== value);
    });
  }

  isPreferenceSelected(
    value: InvestmentPreference
  ): boolean {

    return this.selectedPreferences().includes(value);
  }

  // ---------------------------------------
  // OBJECTIVE SELECTION
  // ---------------------------------------

  toggleObjective(
    value: FinancialObjective,
    checked: boolean
  ): void {

    this.selectedObjectives.update(current => {

      if (checked) {

        return current.includes(value)
          ? current
          : [...current, value];

      }

      return current.filter(item => item !== value);
    });
  }

  isObjectiveSelected(
    value: FinancialObjective
  ): boolean {

    return this.selectedObjectives().includes(value);
  }

  // ---------------------------------------
  // SAVE / UPDATE PROFILE
  // ---------------------------------------

  saveProfile(): void {

    if (this.form.invalid || this.saving()) {

      this.form.markAllAsTouched();
      return;
    }

    if (
      !this.selectedPreferences().length ||
      !this.selectedObjectives().length
    ) {

      this.error.set(
        'Select at least one investment preference and one financial objective.'
      );

      return;
    }

    this.saving.set(true);
    this.error.set('');
    this.message.set('');

    const isNewProfile = !this.hasProfile();

    const request: RiskProfile = {

      ...this.form.getRawValue(),

      investmentPreferences:
        this.selectedPreferences(),

      financialObjectives:
        this.selectedObjectives()

    };

    this.customerApi.saveRiskProfile<RiskProfile>(
      request
    ).subscribe({

      next: response => {

        const savedProfile: RiskProfile = {
          ...request,
          ...response
        };

        this.profile.set(savedProfile);

        this.hasProfile.set(true);

        // Persist customer-specific completion state.
        localStorage.setItem(
          this.getStorageKey(),
          'true'
        );

        this.selectedPreferences.set(
          savedProfile.investmentPreferences
        );

        this.selectedObjectives.set(
          savedProfile.financialObjectives
        );

        this.saving.set(false);

        this.showWelcome.set(false);
        this.showForm.set(false);

        this.message.set(
          isNewProfile
            ? 'Your risk profile has been created successfully.'
            : 'Your risk profile has been updated successfully.'
        );
      },

      error: err => {

        this.saving.set(false);

        this.error.set(
          err?.error?.message ||
          'Unable to save your risk profile. Please try again.'
        );

        // Handle duplicate profile response.
        if (err?.status === 409) {
          this.loadProfile();
        }
      }
    });
  }

  // ---------------------------------------
  // RESET FORM
  // ---------------------------------------

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

  // ---------------------------------------
  // LABEL HELPERS
  // ---------------------------------------

  label(value: string): string {

    return value
      .toLowerCase()
      .replaceAll('_', ' ')
      .replace(/\b\w/g, c => c.toUpperCase());
  }
}
