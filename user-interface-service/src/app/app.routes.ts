import { Routes } from '@angular/router';
import { LedgerPage } from './presentation/ledger/ledger/ledger-page/ledger-page.component';
import { AccountsPageComponent } from './presentation/ledger/accounts/accounts-page/accounts-page.component';
import { AccountTypesPageComponent } from './presentation/ledger/accounts/account-types-page/account-types-page.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'ledger' },
  { path: 'ledger', component: LedgerPage },
  { path: 'accounts', component: AccountsPageComponent },
  { path: 'account-types', component: AccountTypesPageComponent },
];
