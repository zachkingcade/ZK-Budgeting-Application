import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AccountsApplicationService } from './application/ledger/accounts.application-service';
import { PageCage } from "./presentation/page-cage/page-cage";
import { LedgerTable } from "./presentation/ledger/ledger/ledger-table/ledger-table";
import { LedgerPage } from "./presentation/ledger/ledger/ledger-page/ledger-page";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, PageCage, LedgerTable, LedgerPage],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  constructor(private readonly _accountsApplicationService: AccountsApplicationService) {}

  protected readonly title = signal('user-interface-service');
}
