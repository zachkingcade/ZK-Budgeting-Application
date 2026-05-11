import { CommonModule } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { AccountTypeRowView } from '../account-types-filter-state';
import { TableRowActionsMenuComponent } from '../../../shared/table-row-actions-menu/table-row-actions-menu.component';
import { ITableRowAction } from '../../../shared/table-row-actions-menu/table-row-action.types';

@Component({
  selector: 'app-account-types-table',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    TableRowActionsMenuComponent,
  ],
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

  readonly displayedColumns = ['description', 'classification', 'notes', 'rowActions'] as const;

  buildAccountTypesRowActionsMenu(row: AccountTypeRowView): ITableRowAction[] {
    if (row.systemAccount) {
      return [];
    }
    const toggling: boolean = this.togglingTypeId() === row.id;
    return [
      { id: 'edit', label: 'Edit', icon: 'edit', disabled: toggling },
      {
        id: 'toggleActive',
        label: row.active ? 'Disable account type' : 'Enable account type',
        icon: row.active ? 'toggle_on' : 'toggle_off',
        disabled: toggling,
      },
    ];
  }

  onAccountTypesRowMenuAction(selection: { id: string }, row: AccountTypeRowView): void {
    switch (selection.id) {
      case 'edit':
        this.onEdit(row);
        break;
      case 'toggleActive':
        this.onToggleActive(row);
        break;
      default:
        break;
    }
  }

  onEdit(row: AccountTypeRowView): void {
    this.editRequested.emit(row);
  }

  onToggleActive(row: AccountTypeRowView): void {
    this.toggleActiveRequested.emit(row);
  }
}
