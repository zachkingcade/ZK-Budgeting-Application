import { Routes } from '@angular/router';
import { LedgerPage } from './presentation/ledger/ledger/ledger-page/ledger-page.component';
import { AccountsPageComponent } from './presentation/ledger/accounts/accounts-page/accounts-page.component';
import { AccountTypesPageComponent } from './presentation/ledger/accounts/account-types-page/account-types-page.component';
import { ReportsPageComponent } from './presentation/reports/reports-page/reports-page.component';
import { LoginPageComponent } from './presentation/auth/login-page/login-page.component';
import { RegisterPageComponent } from './presentation/auth/register-page/register-page.component';
import { authGuard } from './presentation/auth/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'ledger' },
  { path: 'login', component: LoginPageComponent },
  { path: 'register', component: RegisterPageComponent },
  { path: 'ledger', component: LedgerPage, canMatch: [authGuard] },
  { path: 'accounts', component: AccountsPageComponent, canMatch: [authGuard] },
  { path: 'account-types', component: AccountTypesPageComponent, canMatch: [authGuard] },
  { path: 'reports', component: ReportsPageComponent, canMatch: [authGuard] },
];
