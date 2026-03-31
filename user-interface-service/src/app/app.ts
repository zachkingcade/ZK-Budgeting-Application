import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AccountsApplicationService } from './application/ledger/accounts.application-service';
import { PageCage } from "./presentation/page-cage/page-cage.component";
import { LedgerTable } from "./presentation/ledger/ledger/ledger-table/ledger-table.component";
import { LedgerPage } from "./presentation/ledger/ledger/ledger-page/ledger-page.component";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, PageCage, LedgerTable, LedgerPage],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  constructor(private readonly _accountsApplicationService: AccountsApplicationService) {}

  protected readonly title = signal<string>('user-interface-service');
}
