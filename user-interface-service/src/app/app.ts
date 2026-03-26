import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AccountsApplicationService } from './application/ledger/accounts.application-service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  constructor(private readonly _accountsApplicationService: AccountsApplicationService) {}

  protected readonly title = signal('user-interface-service');
}
