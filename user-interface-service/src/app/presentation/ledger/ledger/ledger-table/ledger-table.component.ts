import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, output, signal, ViewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTable, MatTableModule } from '@angular/material/table';
import { JournalEntryApplicationService } from '../../../../application/ledger/journal-entry.application-service';
import { JournalEntryDTOEnrichedResponse } from '../../../../adapter/ledger-service/dto/journal-entry/JournalEntryDTOEnrichedResponse';
import { JournalLineDTOEnrichedResponse } from '../../../../adapter/ledger-service/dto/journal-entry/JournalLineDTOEnrichedResponse';

@Component({
  selector: 'app-ledger-table',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './ledger-table.component.html',
  styleUrl: './ledger-table.component.scss',
})
export class LedgerTable implements OnInit {
  constructor(
    private readonly journalEntries: JournalEntryApplicationService,
    private readonly destroyRef: DestroyRef,
  ) {}

  @ViewChild(MatTable) private readonly table?: MatTable<JournalEntryDTOEnrichedResponse>;

  readonly journalEntryEdit = output<JournalEntryDTOEnrichedResponse>();
  readonly journalEntryRemove = output<JournalEntryDTOEnrichedResponse>();
  readonly journalLineEdit = output<{
    entry: JournalEntryDTOEnrichedResponse;
    line: JournalLineDTOEnrichedResponse;
  }>();
  readonly journalLineRemove = output<{
    entry: JournalEntryDTOEnrichedResponse;
    line: JournalLineDTOEnrichedResponse;
  }>();

  readonly entries = signal<JournalEntryDTOEnrichedResponse[]>([]);
  readonly loading = signal(false);
  readonly loadError = signal<string | null>(null);
  readonly removingEntryId = signal<number | null>(null);

  readonly displayedColumns = ['entryDate', 'description', 'amount', 'actions'] as const;
  readonly expandedEntryIds = signal<Set<number>>(new Set<number>());

  readonly isExpansionDetailRow = (_index: number, row: JournalEntryDTOEnrichedResponse): boolean =>
    this.expandedEntryIds().has(row.id);

  ngOnInit(): void {
    this.loadEntries();
  }

  loadEntries(): void {
    this.loading.set(true);
    this.loadError.set(null);
    this.journalEntries
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.entries.set(res.data?.journalEntryList ?? []);
          this.loading.set(false);
        },
        error: () => {
          this.loadError.set('Could not load journal entries.');
          this.loading.set(false);
        },
      });
  }

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

  onEditEntry(entry: JournalEntryDTOEnrichedResponse): void {
    this.journalEntryEdit.emit(entry);
  }

  onRemoveEntry(entry: JournalEntryDTOEnrichedResponse): void {
    if (!confirm(`Remove journal entry "${entry.description}"?`)) {
      return;
    }
    this.removingEntryId.set(entry.id);
    this.journalEntries
      .removeById(entry.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.removingEntryId.set(null);
          this.expandedEntryIds.update((prev) => {
            const next = new Set(prev);
            next.delete(entry.id);
            return next;
          });
          this.journalEntryRemove.emit(entry);
          this.loadEntries();
        },
        error: () => {
          this.removingEntryId.set(null);
          this.loadError.set('Could not remove that journal entry.');
        },
      });
  }

  onEditLine(entry: JournalEntryDTOEnrichedResponse, line: JournalLineDTOEnrichedResponse): void {
    this.journalLineEdit.emit({ entry, line });
  }

  onRemoveLine(entry: JournalEntryDTOEnrichedResponse, line: JournalLineDTOEnrichedResponse): void {
    this.journalLineRemove.emit({ entry, line });
  }
}
