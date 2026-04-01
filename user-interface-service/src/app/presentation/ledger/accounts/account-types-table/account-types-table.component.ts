import { CommonModule } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { AccountTypeRowView } from '../account-types-filter-state';

@Component({
  selector: 'app-account-types-table',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './account-types-table.component.html',
  styleUrl: './account-types-table.component.scss',
})
export class AccountTypesTableComponent {
  readonly accountTypes = input.required<AccountTypeRowView[]>();
  readonly loading = input.required<boolean>();
  readonly loadError = input.required<string | null>();
  readonly togglingTypeId = input<number | null>(null);

  readonly editRequested = output<AccountTypeRowView>();
  readonly toggleActiveRequested = output<AccountTypeRowView>();

  readonly displayedColumns = ['description', 'classification', 'notes', 'actions'] as const;

  onEdit(row: AccountTypeRowView): void {
    this.editRequested.emit(row);
  }

  onToggleActive(row: AccountTypeRowView): void {
    this.toggleActiveRequested.emit(row);
  }
}
