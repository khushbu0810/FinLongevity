import { Routes } from "@angular/router";
import { Login } from "./auth/login/login";
import { Register } from "./auth/register/register";
import { guestGuard } from "./guard/guest-guard";
import { Layout } from "./component/layout/layout";
import { authGuardGuard } from "./guard/auth-guard-guard";
import { Dashboard } from "./component/dashboard/dashboard";
import { ProfileComponent } from "./component/profile-component/profile-component";
import { EmploymentComponent } from "./component/employment-component/employment-component";
import { IncomeComponent } from "./component/income-component/income-component";
import { ExpenseComponent } from "./component/expense-component/expense-component";
import { Placeholder } from "./component/placeholder/placeholder";
import { HouseholdComponent } from "./component/household-component/household-component";
import { NomineesComponent } from "./component/nominees-component/nominees-component";
import { InvestmentComponent } from "./component/investment-component/investment-component";
import { PaymentsComponent } from "./component/payments-component/payments-component";
import { RiskProfileComponent } from "./component/risk-profile-component/risk-profile-component";
import { LiabilitiesComponent } from "./component/liabilities-component/liabilities-component";
import { CatalogueComponent } from "./component/catalogue-component/catalogue-component";
import { AssetsComponent } from "./component/assets-component/assets-component";
import { LandingPage } from "./component/landing-page/landing-page";
import { FinancialEngineComponent } from "./component/financial-engine-component/financial-engine-component";
import { InsuranceComponent } from "./component/insurance-component/insurance-component";
import { GoalComponent } from "./component/goal-component/goal-component";


export const routes: Routes = [
    // PUBLIC LANDING PAGE
    {
        path: '',
        component: LandingPage,
        pathMatch: 'full'
    },
    { path: 'login', component: Login, canActivate: [guestGuard] },
    { path: 'register', component: Register, canActivate: [guestGuard] },
    {
        path: '',
        component: Layout,
        canActivate: [authGuardGuard],
        children: [
            { path: '', pathMatch: 'full', redirectTo: 'dashboard' },

            { path: 'dashboard', component: Dashboard },
            { path: 'profile', component: ProfileComponent },
            { path: 'employment', component: EmploymentComponent },
            { path: 'income', component: IncomeComponent },
            { path: 'expenses', component: ExpenseComponent },

            {
                path: 'assets',
                component: AssetsComponent,
                data: {
                    title: 'Assets',
                    description: 'Keep your financial assets in one place.'
                }
            },
            {
                path: 'liabilities',
                component: LiabilitiesComponent,
                data: {
                    title: 'Liabilities',
                    description: 'Track loans, EMIs and outstanding balances.'
                }
            },
            {
                path: 'risk-profile',
                component: RiskProfileComponent,
                data: {
                    title: 'Risk profile',
                    description: 'Set your investment preferences and financial objectives.'
                }
            },
            {
                path: 'household',
                component: HouseholdComponent,
                data: {
                    title: 'Household',
                    description: 'Manage household details and family members.'
                }
            },
            {
                path: 'nominees',
                component: NomineesComponent,
                data: {
                    title: 'Nominees',
                    description: 'Manage nominee and beneficiary allocations.'
                }
            },
            {
                path: 'investments',
                component: InvestmentComponent,
                data: {
                    title: 'Investments',
                    description: 'View investments and investment transactions.'
                }
            },
            {
                path: 'payments',
                component: PaymentsComponent,
                data: {
                    title: 'Payments',
                    description: 'Review payment activity and payment events.'
                }
            },
            {
                path: 'catalogue',
                component: CatalogueComponent,
                data: {
                    title: 'Product catalogue',
                    description: 'Explore providers and their financial products.'
                }
            },
            {
                path: 'goals',
                component: GoalComponent,
                data: {
                    title: 'Goals',
                    description: 'Create financial goals, track progress, manage contributions and monitor group goals.'
                }
            },
            {
                path: 'insurance',
                component: InsuranceComponent,
                data: {
                    title: 'Insurance',
                    description: 'Manage insurance applications, track application decisions and view active policies.'
                }
            },
            {
                path: 'financial-engine',
                component: FinancialEngineComponent,
                data: {
                    title: 'Financial Engine',
                    description: 'Analyze financial health, track longevity scores and plan retirement projections.'
                }
            }
        ]
    },
    { path: '**', redirectTo: '' }
];