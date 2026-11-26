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


export const routes: Routes = [
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
                component: Placeholder,
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
            }
        ]
    },
    { path: '**', redirectTo: '' }
];