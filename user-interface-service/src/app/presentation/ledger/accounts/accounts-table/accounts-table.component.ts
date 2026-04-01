import { CommonModule } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { AccountEnrichedObject } from '../../../../adapter/ledger-service/dto/account/AccountEnrichedObject';

@Component({
  selector: 'app-accounts-table',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './accounts-table.component.html',
  styleUrl: './accounts-table.component.scss',
})
export class AccountsTableComponent {
  readonly accounts = input.required<AccountEnrichedObject[]>();
  readonly loading = input.required<boolean>();
  readonly loadError = input.required<string | null>();
  readonly togglingAccountId = input<number | null>(null);

  readonly editRequested = output<AccountEnrichedObject>();
  readonly toggleActiveRequested = output<AccountEnrichedObject>();

  readonly displayedColumns = [
    'description',
    'accountTypeName',
    'accountBalance',
    'notes',
    'actions',
  ] as const;

  formatMoney(minorUnits: number): string {
    const major = minorUnits / 100;
    return new Intl.NumberFormat(undefined, { style: 'currency', currency: 'USD' }).format(major);
  }

  onEdit(account: AccountEnrichedObject): void {
    this.editRequested.emit(account);
  }

  onToggleActive(account: AccountEnrichedObject): void {
    this.toggleActiveRequested.emit(account);
  }
}
