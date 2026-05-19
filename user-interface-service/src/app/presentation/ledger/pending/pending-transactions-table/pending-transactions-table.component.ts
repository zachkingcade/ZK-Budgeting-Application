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
  formatMoneyDollarsOnBlur,
  IJournalEntryLineDraft,
  onMoneyDollarsKeydown,
  sanitizeMoneyDollarsInput,
  validateJournalEntryDraft,
} from '../../../../domain/journal-entry/journal-entry.validation';
import { TableRowActionsMenuComponent } from '../../../shared/table-row-actions-menu/table-row-actions-menu.component';
import { ITableRowAction } from '../../../shared/table-row-actions-menu/table-row-action.types';

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
    TableRowActionsMenuComponent,
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
  readonly changeDateRequested = output<PendingTransactionObject>();
  readonly saveAsReplayableRequested = output<PendingTransactionObject>();
  readonly loadReplayableRequested = output<PendingTransactionObject>();
  readonly draftChanged = output<{ transactionNumber: number; draft: IPendingDraft }>();

  readonly displayedColumns = [
    'date',
    'description',
    'amount',
    'debitAccount',
    'creditAccount',
    'rowActions',
    'expand',
  ] as const;
  readonly expandedIds = signal<Set<number>>(new Set<number>());

  readonly isExpansionDetailRow = (_index: number, row: PendingTransactionObject): boolean =>
    this.expandedIds().has(row.transactionNumber);

  buildPendingRowActionsMenu(row: PendingTransactionObject): ITableRowAction[] {
    return [
      {
        id: 'loadReplayable',
        label: 'Load replayable',
        icon: 'playlist_play',
        disabled: this.removingTransactionNumber() === row.transactionNumber,
      },
      {
        id: 'saveAsReplayable',
        label: 'Save as replayable',
        icon: 'bookmark_add',
        disabled: this.removingTransactionNumber() === row.transactionNumber,
      },
      {
        id: 'changeDate',
        label: 'Change date',
        icon: 'event',
        disabled: this.removingTransactionNumber() === row.transactionNumber,
      },
      {
        id: 'delete',
        label: 'Delete',
        icon: 'delete',
        disabled: this.removingTransactionNumber() === row.transactionNumber,
      },
    ];
  }

  onPendingRowMenuAction(selection: { id: string }, row: PendingTransactionObject): void {
    switch (selection.id) {
      case 'loadReplayable':
        this.loadReplayableRequested.emit(row);
        break;
      case 'saveAsReplayable':
        this.saveAsReplayableRequested.emit(row);
        break;
      case 'changeDate':
        this.changeDateRequested.emit(row);
        break;
      case 'delete':
        this.deleteRequested.emit(row);
        break;
      default:
        break;
    }
  }

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
    queueMicrotask(() => this.table?.renderRows());
  }

  debitLine(tx: PendingTransactionObject): IJournalEntryLineDraft | null {
    const d = this.getDraft(tx);
    return d?.lines?.[0] ?? null;
  }

  creditLine(tx: PendingTransactionObject): IJournalEntryLineDraft | null {
    const d = this.getDraft(tx);
    return d?.lines?.[1] ?? null;
  }

  showCompactDebitCreditSelectors(tx: PendingTransactionObject): boolean {
    const d = this.getDraft(tx);
    if (!d) return false;
    const lines = d.lines ?? [];
    if (lines.length !== 2) return false;
    const l0 = lines[0];
    const l1 = lines[1];
    if (l0.direction !== 'D' || l1.direction !== 'C') return false;
    const m0 = dollarsStringToMinorUnits(l0.amountDollars);
    const m1 = dollarsStringToMinorUnits(l1.amountDollars);
    if (m0 == null || m1 == null) return false;
    return m0 === tx.amount && m1 === tx.amount;
  }

  lineDirectionCounts(tx: PendingTransactionObject): { debit: number; credit: number } {
    const d = this.getDraft(tx);
    if (!d) return { debit: 0, credit: 0 };
    let debit = 0;
    let credit = 0;
    for (const line of d.lines ?? []) {
      if (line.direction === 'D') debit++;
      else if (line.direction === 'C') credit++;
    }
    return { debit, credit };
  }

  debitCountSummary(tx: PendingTransactionObject): string {
    const n = this.lineDirectionCounts(tx).debit;
    return n === 1 ? '1 debit' : `${n} debits`;
  }

  creditCountSummary(tx: PendingTransactionObject): string {
    const n = this.lineDirectionCounts(tx).credit;
    return n === 1 ? '1 credit' : `${n} credits`;
  }

  /** Ledger effect sign for the compact row line (green + / red −), once account + direction are set. */
  compactAccountEffectSign(line: IJournalEntryLineDraft | null): '+' | '-' | null {
    if (!line || line.accountId == null || (line.direction !== 'C' && line.direction !== 'D')) {
      return null;
    }
    const acc = this.accountsById().get(line.accountId);
    if (!acc) return null;
    const sign = line.direction === 'C' ? acc.creditEffect : acc.debitEffect;
    if (sign === '+') return '+';
    if (sign === '-') return '-';
    return null;
  }

  onCompactDebitAccountChange(tx: PendingTransactionObject, accountId: number | null): void {
    const d = this.getDraft(tx);
    if (!d || d.lines.length < 2) return;
    const lines = d.lines.map((l, i) => {
      if (i === 0) return { ...l, accountId, direction: 'D' as const };
      if (i === 1) return { ...l, direction: 'C' as const };
      return l;
    });
    this.setDraft(tx, { ...d, lines });
  }

  onCompactCreditAccountChange(tx: PendingTransactionObject, accountId: number | null): void {
    const d = this.getDraft(tx);
    if (!d || d.lines.length < 2) return;
    const lines = d.lines.map((l, i) => {
      if (i === 0) return { ...l, direction: 'D' as const };
      if (i === 1) return { ...l, accountId, direction: 'C' as const };
      return l;
    });
    this.setDraft(tx, { ...d, lines });
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

  onLineAmountChange(tx: PendingTransactionObject, lineIndex: number, value: string): void {
    const d = this.getDraft(tx);
    if (!d) return;
    const lines = d.lines.map((l, i) =>
      i === lineIndex ? { ...l, amountDollars: sanitizeMoneyDollarsInput(value) } : l,
    );
    this.setDraft(tx, { ...d, lines });
  }

  onLineAmountBlur(tx: PendingTransactionObject, lineIndex: number): void {
    const d = this.getDraft(tx);
    if (!d) return;
    const lines = d.lines.map((l, i) =>
      i === lineIndex ? { ...l, amountDollars: formatMoneyDollarsOnBlur(l.amountDollars) } : l,
    );
    this.setDraft(tx, { ...d, lines });
  }

  onLineAmountInput(tx: PendingTransactionObject, lineIndex: number, ev: Event): void {
    const t = ev.target;
    if (!(t instanceof HTMLInputElement)) {
      return;
    }
    this.onLineAmountChange(tx, lineIndex, sanitizeMoneyDollarsInput(t.value));
  }

  onMoneyKeydown(ev: KeyboardEvent): void {
    onMoneyDollarsKeydown(ev);
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

