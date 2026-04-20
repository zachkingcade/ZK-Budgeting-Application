import { CommonModule } from '@angular/common';
import { Component, ViewChild, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTable, MatTableModule } from '@angular/material/table';
import { MtxSelectModule } from '@ng-matero/extensions/select';
import { PendingTransactionObject } from '../../../../adapter/ledger-service/dto/pending-transaction/PendingTransactionObject';
import { AccountEnrichedObject } from '../../../../adapter/ledger-service/dto/account/AccountEnrichedObject';
import {
  dollarsStringToMinorUnits,
  IJournalEntryLineDraft,
  validateJournalEntryDraft,
} from '../../../../domain/journal-entry/journal-entry.validation';

type SelectOption<T extends string | number> = { id: T; label: string };

export interface IPendingDraft {
  entryDate: string;
  description: string;
  notes: string;
  lines: IJournalEntryLineDraft[];
}

@Component({
  selector: 'app-pending-transactions-table',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MtxSelectModule,
  ],
  templateUrl: './pending-transactions-table.component.html',
  styleUrl: './pending-transactions-table.component.scss',
})
export class PendingTransactionsTableComponent {
  @ViewChild(MatTable) private readonly table?: MatTable<PendingTransactionObject>;

  readonly transactions = input.required<PendingTransactionObject[]>();
  readonly loading = input.required<boolean>();
  readonly loadError = input.required<string | null>();
  readonly removingTransactionNumber = input.required<number | null>();

  readonly accountOptions = input.required<SelectOption<number>[]>();
  readonly accountsById = input.required<Map<number, AccountEnrichedObject>>();

  // Drafts are owned by the parent; we mutate via events.
  readonly draftsById = input.required<Map<number, IPendingDraft>>();

  readonly deleteRequested = output<PendingTransactionObject>();
  readonly draftChanged = output<{ transactionNumber: number; draft: IPendingDraft }>();

  readonly displayedColumns = ['date', 'description', 'amount', 'notes', 'actions'] as const;
  readonly expandedIds = signal<Set<number>>(new Set<number>());

  readonly isExpansionDetailRow = (_index: number, row: PendingTransactionObject): boolean =>
    this.expandedIds().has(row.transactionNumber);

  toggleDetails(tx: PendingTransactionObject): void {
    this.expandedIds.update((prev) => {
      const next = new Set(prev);
      if (next.has(tx.transactionNumber)) next.delete(tx.transactionNumber);
      else next.add(tx.transactionNumber);
      return next;
    });
    this.table?.renderRows();
  }

  detailsOpen(tx: PendingTransactionObject): boolean {
    return this.expandedIds().has(tx.transactionNumber);
  }

  getDraft(tx: PendingTransactionObject): IPendingDraft | null {
    return this.draftsById().get(tx.transactionNumber) ?? null;
  }

  setDraft(tx: PendingTransactionObject, next: IPendingDraft): void {
    this.draftChanged.emit({ transactionNumber: tx.transactionNumber, draft: next });
  }

  addRow(tx: PendingTransactionObject): void {
    const d = this.getDraft(tx);
    if (!d) return;
    this.setDraft(tx, { ...d, lines: [...d.lines, { amountDollars: '', accountId: null, direction: '', notes: '' }] });
  }

  removeRow(tx: PendingTransactionObject, index: number): void {
    const d = this.getDraft(tx);
    if (!d) return;
    if (d.lines.length <= 2) return;
    this.setDraft(tx, { ...d, lines: d.lines.filter((_l, i) => i !== index) });
  }

  debitTotalMinor(tx: PendingTransactionObject): number {
    const d = this.getDraft(tx);
    if (!d) return 0;
    let sum = 0;
    for (const line of d.lines) {
      if (line.direction !== 'D') continue;
      const m = dollarsStringToMinorUnits(line.amountDollars);
      if (m != null) sum += m;
    }
    return sum;
  }

  creditTotalMinor(tx: PendingTransactionObject): number {
    const d = this.getDraft(tx);
    if (!d) return 0;
    let sum = 0;
    for (const line of d.lines) {
      if (line.direction !== 'C') continue;
      const m = dollarsStringToMinorUnits(line.amountDollars);
      if (m != null) sum += m;
    }
    return sum;
  }

  remainingMinor(tx: PendingTransactionObject): number {
    return tx.amount - this.debitTotalMinor(tx);
  }

  validatePending(tx: PendingTransactionObject): { valid: boolean; message: string | null } {
    const d = this.getDraft(tx);
    if (!d) return { valid: false, message: 'Missing draft.' };
    const desc = (d.description ?? '').trim();
    if (desc.length === 0) return { valid: false, message: 'Description is required.' };

    const base = validateJournalEntryDraft(d.lines);
    if (!base.valid) {
      // UX: don't show this specific message inline (it's noisy while picking accounts).
      if (base.errorMessage === 'Each line requires an account.') {
        return { valid: false, message: null };
      }
      return { valid: false, message: base.errorMessage };
    }

    const debit = this.debitTotalMinor(tx);
    const credit = this.creditTotalMinor(tx);
    if (debit !== tx.amount || credit !== tx.amount) {
      return { valid: false, message: 'Debit and credit totals must equal the pending amount.' };
    }

    return { valid: true, message: null };
  }

  altRowForTransaction(tx: PendingTransactionObject): boolean {
    const idx = (this.transactions() ?? []).findIndex((t) => t.transactionNumber === tx.transactionNumber);
    return idx >= 0 && idx % 2 === 1;
  }

  formatDate(iso: string): string {
    if (!iso) return '';
    const d = new Date(iso + 'T12:00:00');
    return new Intl.DateTimeFormat(undefined, { year: 'numeric', month: 'short', day: 'numeric' }).format(d);
  }

  formatMoney(minorUnits: number): string {
    const major = minorUnits / 100;
    return new Intl.NumberFormat(undefined, { style: 'currency', currency: 'USD' }).format(major);
  }

  directionOptions: SelectOption<'C' | 'D'>[] = [
    { id: 'D', label: 'Debit' },
    { id: 'C', label: 'Credit' },
  ];

  lineAffectClass(line: IJournalEntryLineDraft): 'affect-positive' | 'affect-negative' | '' {
    if (line.accountId == null || (line.direction !== 'C' && line.direction !== 'D')) {
      return '';
    }
    const acc = this.accountsById().get(line.accountId);
    if (!acc) {
      return '';
    }
    const sign = line.direction === 'C' ? acc.creditEffect : acc.debitEffect;
    return sign === '+' ? 'affect-positive' : 'affect-negative';
  }

  lineAffectLabel(line: IJournalEntryLineDraft): string {
    if (line.accountId == null || (line.direction !== 'C' && line.direction !== 'D')) {
      return '—';
    }
    const acc = this.accountsById().get(line.accountId);
    if (!acc) {
      return '—';
    }
    const sign = line.direction === 'C' ? acc.creditEffect : acc.debitEffect;
    const dir = line.direction === 'C' ? 'Credit' : 'Debit';
    return `${dir} (${sign})`;
  }
}

