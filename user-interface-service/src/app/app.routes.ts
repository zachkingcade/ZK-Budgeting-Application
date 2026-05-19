import { Routes } from '@angular/router';
import { LedgerPage } from './presentation/ledger/ledger/ledger-page/ledger-page.component';
import { AccountsPageComponent } from './presentation/ledger/accounts/accounts-page/accounts-page.component';
import { AccountTypesPageComponent } from './presentation/ledger/accounts/account-types-page/account-types-page.component';
import { ReportsPageComponent } from './presentation/reports/reports-page/reports-page.component';
import { LoginPageComponent } from './presentation/auth/login-page/login-page.component';
import { RegisterPageComponent } from './presentation/auth/register-page/register-page.component';
import { authGuard } from './presentation/auth/auth.guard';
import { PendingJournalEntriesPageComponent } from './presentation/ledger/pending/pending-journal-entries-page/pending-journal-entries-page.component';
import { BudgetPlanningPageComponent } from './presentation/planning/budget-planning-page/budget-planning-page.component';
import { ReplayableJournalEntriesPageComponent } from './presentation/planning/replayable-journal-entries-page/replayable-journal-entries-page.component';
import { AccountingPeriodsPageComponent } from './presentation/ledger/accounting-periods-page/accounting-periods-page.component';
import { SettingsPageComponent } from './presentation/user-account/settings-page/settings-page.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'ledger' },
  { path: 'login', component: LoginPageComponent },
  { path: 'register', component: RegisterPageComponent },
  { path: 'ledger', component: LedgerPage, canMatch: [authGuard] },
  { path: 'pending-journal-entries', component: PendingJournalEntriesPageComponent, canMatch: [authGuard] },
  { path: 'accounts', component: AccountsPageComponent, canMatch: [authGuard] },
  { path: 'account-types', component: AccountTypesPageComponent, canMatch: [authGuard] },
  { path: 'reports', component: ReportsPageComponent, canMatch: [authGuard] },
  { path: 'budget-planning', component: BudgetPlanningPageComponent, canMatch: [authGuard] },
  { path: 'replayable-journal-entries', component: ReplayableJournalEntriesPageComponent, canMatch: [authGuard] },
  { path: 'accounting-periods', component: AccountingPeriodsPageComponent, canMatch: [authGuard] },
  { path: 'settings', component: SettingsPageComponent, canMatch: [authGuard] },
];
