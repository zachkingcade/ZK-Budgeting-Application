import { CommonModule } from '@angular/common';
import { Component, input, output, signal, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTable, MatTableModule } from '@angular/material/table';
import { JournalEntryDTOEnrichedResponse } from '../../../../adapter/ledger-service/dto/journal-entry/JournalEntryDTOEnrichedResponse';
import { JournalLineDTOEnrichedResponse } from '../../../../adapter/ledger-service/dto/journal-entry/JournalLineDTOEnrichedResponse';
import { TableRowActionsMenuComponent } from '../../../shared/table-row-actions-menu/table-row-actions-menu.component';
import { ITableRowAction } from '../../../shared/table-row-actions-menu/table-row-action.types';

@Component({
  selector: 'app-ledger-table',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    TableRowActionsMenuComponent,
  ],
  templateUrl: './ledger-table.component.html',
  styleUrl: './ledger-table.component.scss',
})
export class LedgerTable {
  @ViewChild(MatTable) private readonly table?: MatTable<JournalEntryDTOEnrichedResponse>;

  readonly entries = input.required<JournalEntryDTOEnrichedResponse[]>();
  readonly loading = input.required<boolean>();
  readonly loadError = input.required<string | null>();
  readonly removingEntryId = input.required<number | null>();

  readonly editRequested = output<JournalEntryDTOEnrichedResponse>();
  readonly deleteRequested = output<JournalEntryDTOEnrichedResponse>();
  readonly saveAsReplayableRequested = output<JournalEntryDTOEnrichedResponse>();

  readonly displayedColumns = ['entryDate', 'description', 'amount', 'rowActions', 'expand'] as const;
  readonly expandedEntryIds = signal<Set<number>>(new Set<number>());

  readonly isExpansionDetailRow = (_index: number, row: JournalEntryDTOEnrichedResponse): boolean =>
    this.expandedEntryIds().has(row.id);

  toggleDetails(entry: JournalEntryDTOEnrichedResponse): void {
    this.expandedEntryIds.update((prev) => {
      const next = new Set(prev);
      if (next.has(entry.id)) {
        next.delete(entry.id);
      } else {
        next.add(entry.id);
      }
      return next;
    });
    // MatTable doesn't automatically re-evaluate rowDef `when` predicates on signal changes.
    this.table?.renderRows();
  }

  detailsOpen(entry: JournalEntryDTOEnrichedResponse): boolean {
    return this.expandedEntryIds().has(entry.id);
  }

  altRowForEntry(entry: JournalEntryDTOEnrichedResponse): boolean {
    const idx = (this.entries() ?? []).findIndex((e) => e.id === entry.id);
    return idx >= 0 && idx % 2 === 1;
  }

  formatDate(isoDate: string): string {
    if (!isoDate) {
      return '';
    }
    const d = new Date(isoDate + 'T12:00:00');
    return new Intl.DateTimeFormat(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    }).format(d);
  }

  formatMoney(minorUnits: number): string {
    // Amounts from the API are stored in minor units (e.g. cents)
    const major = minorUnits / 100;
    return new Intl.NumberFormat(undefined, { style: 'currency', currency: 'USD' }).format(major);
  }

  entryDebitTotal(entry: JournalEntryDTOEnrichedResponse): number {
    return entry.journalLines
      .filter((l) => l.direction === 'D')
      .reduce((sum, l) => sum + l.amount, 0);
  }

  directionLabel(direction: string): string {
    return direction === 'C' ? 'Credit' : 'Debit';
  }

  lineAffectClass(line: JournalLineDTOEnrichedResponse): 'affect-positive' | 'affect-negative' {
    return line.lineAffectOnAccount === '+' ? 'affect-positive' : 'affect-negative';
  }

  buildLedgerRowActionsMenu(entry: JournalEntryDTOEnrichedResponse): ITableRowAction[] {
    return [
      { id: 'saveAsReplayable', label: 'Save as replayable', icon: 'bookmark_add' },
      { id: 'edit', label: 'Edit', icon: 'edit' },
      {
        id: 'delete',
        label: 'Delete',
        icon: 'delete',
        disabled: this.removingEntryId() === entry.id,
      },
    ];
  }

  onLedgerRowMenuAction(selection: { id: string }, entry: JournalEntryDTOEnrichedResponse): void {
    switch (selection.id) {
      case 'saveAsReplayable':
        this.saveAsReplayableRequested.emit(entry);
        break;
      case 'edit':
        this.onEditEntry(entry);
        break;
      case 'delete':
        this.onRemoveEntry(entry);
        break;
      default:
        break;
    }
  }

  onEditEntry(entry: JournalEntryDTOEnrichedResponse): void {
    this.editRequested.emit(entry);
  }

  onRemoveEntry(entry: JournalEntryDTOEnrichedResponse): void {
    this.deleteRequested.emit(entry);
  }

  onEditLine(entry: JournalEntryDTOEnrichedResponse, line: JournalLineDTOEnrichedResponse): void {
    // Not implemented in this feature scope (entry-level edit modal covers line notes).
    void entry;
    void line;
  }

  onRemoveLine(entry: JournalEntryDTOEnrichedResponse, line: JournalLineDTOEnrichedResponse): void {
    // Not implemented in this feature scope.
    void entry;
    void line;
  }
}
