import { Component, DestroyRef, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { readLedgerApiErrorMessage } from '../../../../adapter/ledger-service/ledger-http-error.util';
import { PageCage } from '../../../page-cage/page-cage.component';
import { ConfirmationModalComponent } from '../../../shared/confirmation-modal/confirmation-modal.component';
import { PendingImportBarComponent } from '../pending-import-bar/pending-import-bar.component';
import { PendingTransactionsTableComponent, IPendingDraft } from '../pending-transactions-table/pending-transactions-table.component';
import { ImportFormatsApplicationService } from '../../../../application/ledger/import-formats.application-service';
import { PendingTransactionsApplicationService } from '../../../../application/ledger/pending-transactions.application-service';
import { ImportFormatObject } from '../../../../adapter/ledger-service/dto/import-format/ImportFormatObject';
import { PendingTransactionObject } from '../../../../adapter/ledger-service/dto/pending-transaction/PendingTransactionObject';
import { AccountsApplicationService } from '../../../../application/ledger/accounts.application-service';
import { AccountEnrichedObject } from '../../../../adapter/ledger-service/dto/account/AccountEnrichedObject';
import { ToastService } from '../../../../application/toast.service';
import { POSTApplyPendingTransactionsRequest } from '../../../../adapter/ledger-service/dto/pending-transaction/apply/POSTApplyPendingTransactionsRequest';
import {
  dollarsStringToMinorUnits,
  IJournalEntryLineDraft,
  minorUnitsToDollarsString,
  validateJournalEntryDraft,
} from '../../../../domain/journal-entry/journal-entry.validation';
import { ApplyPendingTransactionsFailureObject } from '../../../../adapter/ledger-service/dto/pending-transaction/apply/ApplyPendingTransactionsFailureObject';
import {
  ChangePendingDateDialogComponent,
  ChangePendingDateDialogResult,
} from '../change-pending-date-dialog.component';
import {
  SaveAsReplayableDialogComponent,
  SaveAsReplayableDialogData,
} from '../save-as-replayable-dialog.component';
import {
  LoadReplayableJournalEntryDialogComponent,
  LoadReplayableJournalEntryDialogResult,
} from '../../../shared/load-replayable-journal-entry-dialog/load-replayable-journal-entry-dialog.component';

type SelectOption<T extends string | number> = { id: T; label: string };

import { ContextHelpButtonComponent } from '../../../../help/components/context-help-button/context-help-button.component';
@Component({
  selector: 'app-pending-journal-entries-page',
  standalone: true,
  imports: [
    ContextHelpButtonComponent,
    PageCage,
    ConfirmationModalComponent,
    PendingImportBarComponent,
    PendingTransactionsTableComponent,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './pending-journal-entries-page.component.html',
  styleUrl: './pending-journal-entries-page.component.scss',
})
export class PendingJournalEntriesPageComponent implements OnInit {
  constructor(
    private readonly importFormats: ImportFormatsApplicationService,
    private readonly pendingTransactions: PendingTransactionsApplicationService,
    private readonly accounts: AccountsApplicationService,
    private readonly toast: ToastService,
    private readonly dialog: MatDialog,
    private readonly destroyRef: DestroyRef,
  ) {}

  readonly formats = signal<ImportFormatObject[]>([]);
  readonly formatsLoading = signal<boolean>(false);

  readonly selectedFormatId = signal<number | null>(null);
  readonly selectedFile = signal<File | null>(null);
  readonly selectedFileName = signal<string | null>(null);
  readonly importing = signal<boolean>(false);

  readonly txs = signal<PendingTransactionObject[]>([]);
  readonly loading = signal<boolean>(false);
  readonly loadError = signal<string | null>(null);
  readonly removingTransactionNumber = signal<number | null>(null);

  readonly draftsById = signal<Map<number, IPendingDraft>>(new Map());

  readonly accountOptions = signal<SelectOption<number>[]>([]);
  readonly accountsById = signal<Map<number, AccountEnrichedObject>>(new Map());

  readonly applying = signal<boolean>(false);
  readonly applyFailures = signal<{ label: string; message: string }[]>([]);

  readonly deleteConfirmOpen = signal<boolean>(false);
  readonly modalError = signal<string | null>(null);
  readonly pendingTransactionPendingDelete = signal<PendingTransactionObject | null>(null);

  ngOnInit(): void {
    this.loadFormats();
    this.loadAccounts();
    this.refresh();
  }

  private loadFormats(): void {
    this.formatsLoading.set(true);
    this.importFormats
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.formats.set(res.data?.importFormats ?? []);
          this.formatsLoading.set(false);
        },
        error: () => {
          this.formats.set([]);
          this.formatsLoading.set(false);
        },
      });
  }

  private loadAccounts(): void {
    this.accounts
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const list: AccountEnrichedObject[] = res.data?.accountsList ?? [];
          const map = new Map<number, AccountEnrichedObject>();
          for (const a of list) map.set(a.accountId, a);
          this.accountsById.set(map);
          this.accountOptions.set(
            list.filter((a) => a.active).map((a) => ({ id: a.accountId, label: a.accountDisplayName })),
          );
        },
        error: () => {
          this.accountsById.set(new Map());
          this.accountOptions.set([]);
        },
      });
  }

  refresh(): void {
    this.loading.set(true);
    this.loadError.set(null);
    this.pendingTransactions
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const list: PendingTransactionObject[] = res.data?.pendingTransactions ?? [];
          this.txs.set(list);
          this.ensureDrafts(list);
          this.loading.set(false);
        },
        error: () => {
          this.loadError.set('Could not load pending transactions.');
          this.loading.set(false);
        },
      });
  }

  private ensureDrafts(list: PendingTransactionObject[]): void {
    this.draftsById.update((prev) => {
      const next = new Map(prev);
      const ids = new Set<number>();
      for (const tx of list) {
        ids.add(tx.transactionNumber);
        if (!next.has(tx.transactionNumber)) {
          next.set(tx.transactionNumber, this.defaultDraftForTx(tx));
        }
      }
      // prune removed txs
      for (const existingId of Array.from(next.keys())) {
        if (!ids.has(existingId)) next.delete(existingId);
      }
      return next;
    });
  }

  private defaultDraftForTx(tx: PendingTransactionObject): IPendingDraft {
    const baseNotes = (tx.notes ?? '').trim();
    const amountStr = minorUnitsToDollarsString(tx.amount);
    return {
      entryDate: tx.transactionDate,
      description: tx.description,
      notes: baseNotes,
      lines: [
        { amountDollars: amountStr, accountId: null, direction: 'D', notes: '' },
        { amountDollars: amountStr, accountId: null, direction: 'C', notes: '' },
      ],
    };
  }

  onFormatChanged(next: number | null): void {
    this.selectedFormatId.set(next);
  }

  onFilePicked(file: File): void {
    this.selectedFile.set(file);
    this.selectedFileName.set(file.name);
  }

  onImportClicked(): void {
    if (this.importing()) return;
    const formatId = this.selectedFormatId();
    const file = this.selectedFile();
    if (formatId == null || !file) return;

    this.importing.set(true);
    this.pendingTransactions
      .import(formatId, file)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const created = res.data?.createdCount ?? 0;
          this.toast.showSuccess(`Imported ${created} pending transactions.`);
          this.importing.set(false);
          this.refresh();
        },
        error: () => {
          this.importing.set(false);
          this.toast.showError('Import failed. Make sure the format and CSV headers match.');
        },
      });
  }

  openSaveAsReplayable(tx: PendingTransactionObject): void {
    const draft = this.draftsById().get(tx.transactionNumber);
    if (!draft) {
      this.toast.showError('No journal lines to save.');
      return;
    }
    const validation = this.validateDraftForTx(tx, draft);
    if (!validation.valid) {
      this.toast.showError(validation.message ?? 'Complete and balance journal lines before saving as a replay.');
      return;
    }
    const data: SaveAsReplayableDialogData = {
      defaultName: (draft.description ?? tx.description).trim() || tx.description,
      lines: draft.lines,
    };
    this.dialog
      .open(SaveAsReplayableDialogComponent, { data, width: '28rem' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.toast.showSuccess('Saved as replayable journal entry.');
        }
      });
  }

  openLoadReplayable(tx: PendingTransactionObject): void {
    this.dialog
      .open(LoadReplayableJournalEntryDialogComponent, { width: '36rem' })
      .afterClosed()
      .subscribe((lines: LoadReplayableJournalEntryDialogResult | undefined) => {
        if (!lines || lines.length === 0) {
          return;
        }
        const draft = this.draftsById().get(tx.transactionNumber);
        if (!draft) {
          return;
        }
        this.draftsById.update((prev) => {
          const next = new Map(prev);
          next.set(tx.transactionNumber, { ...draft, lines });
          return next;
        });
        const debit = this.draftDebitTotalMinor({ ...draft, lines });
        const credit = this.draftCreditTotalMinor({ ...draft, lines });
        if (debit !== tx.amount || credit !== tx.amount) {
          this.toast.showError(
            'Loaded amounts were copied from the template and may not match this pending transaction amount.',
          );
        }
      });
  }

  openChangeDateDialog(tx: PendingTransactionObject): void {
    const ref = this.dialog.open(ChangePendingDateDialogComponent, {
      data: { transaction: tx },
      width: '28rem',
    });
    ref.afterClosed().subscribe((result: ChangePendingDateDialogResult | undefined) => {
      if (!result) {
        return;
      }
      if (result.transactionDate === tx.transactionDate) {
        return;
      }
      this.pendingTransactions
        .updateDate(tx.transactionNumber, { transactionDate: result.transactionDate })
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.txs.update((list) =>
              list.map((t) =>
                t.transactionNumber === tx.transactionNumber
                  ? { ...t, transactionDate: result.transactionDate }
                  : t,
              ),
            );
            this.draftsById.update((prev) => {
              const draft = prev.get(tx.transactionNumber);
              if (!draft) {
                return prev;
              }
              const next = new Map(prev);
              next.set(tx.transactionNumber, { ...draft, entryDate: result.transactionDate });
              return next;
            });
            this.toast.showSuccess('Pending transaction date updated.');
          },
          error: (err: unknown) => {
            this.toast.showError(
              readLedgerApiErrorMessage(err, 'Could not update the pending transaction date.'),
            );
          },
        });
    });
  }

  openDeleteConfirm(tx: PendingTransactionObject): void {
    this.modalError.set(null);
    this.pendingTransactionPendingDelete.set(tx);
    this.deleteConfirmOpen.set(true);
  }

  deleteConfirmMessage(): string {
    const tx = this.pendingTransactionPendingDelete();
    if (!tx) {
      return '';
    }
    return `Remove pending transaction "${tx.description}"?`;
  }

  closeDeleteConfirm(): void {
    this.deleteConfirmOpen.set(false);
    this.pendingTransactionPendingDelete.set(null);
    this.modalError.set(null);
  }

  onDeleteConfirmed(): void {
    const tx = this.pendingTransactionPendingDelete();
    if (!tx) {
      this.closeDeleteConfirm();
      return;
    }

    this.removingTransactionNumber.set(tx.transactionNumber);
    this.modalError.set(null);

    this.pendingTransactions
      .removeById(tx.transactionNumber)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.removingTransactionNumber.set(null);
          this.closeDeleteConfirm();
          this.refresh();
        },
        error: () => {
          this.removingTransactionNumber.set(null);
          this.modalError.set('Could not remove that pending transaction.');
        },
      });
  }

  onDraftChanged(evt: { transactionNumber: number; draft: IPendingDraft }): void {
    this.draftsById.update((prev) => {
      const next = new Map(prev);
      next.set(evt.transactionNumber, evt.draft);
      return next;
    });
  }

  applyDisabled(): boolean {
    return this.applying() || this.txs().length === 0;
  }

  onApplyClicked(): void {
    if (this.applying()) return;

    const clientFailures: { label: string; message: string }[] = [];
    const items: POSTApplyPendingTransactionsRequest['items'] = [];
    for (const tx of this.txs()) {
      const draft = this.draftsById().get(tx.transactionNumber);
      if (!draft) continue;

      const hasAnyLineInfo = this.draftHasAnyLineInfo(draft);
      if (!hasAnyLineInfo) {
        // Ignore untouched rows (even if expanded/collapsed).
        continue;
      }

      const validation = this.validateDraftForTx(tx, draft);
      if (!validation.valid) {
        clientFailures.push({
          label: this.failureLabelForTx(tx),
          message: validation.message ?? 'Incomplete or invalid.',
        });
        continue;
      }

      const desc = (draft.description ?? '').trim();
      if (desc.length === 0) {
        clientFailures.push({ label: this.failureLabelForTx(tx), message: 'Description is required.' });
        continue;
      }

      const lines: IJournalEntryLineDraft[] = draft.lines ?? [];
      const journalLines = lines.map((l) => ({
        amount: dollarsStringToMinorUnits(l.amountDollars) as number,
        accountId: l.accountId as number,
        direction: l.direction as 'C' | 'D',
        notes: (l.notes ?? '').trim(),
      }));

      items.push({
        pendingTransactionNumber: tx.transactionNumber,
        entryDate: draft.entryDate,
        description: desc,
        notes: (draft.notes ?? '').trim(),
        journalLines,
      });
    }

    if (items.length === 0) {
      // Only show errors for rows where the user started entering journal line info.
      this.applyFailures.set(clientFailures);
      if (clientFailures.length > 0) {
        this.toast.showError('Some pending transactions are incomplete. Fix the rows highlighted below.');
      } else {
        this.toast.showError('Nothing to apply. Fill out journal lines for at least one pending transaction.');
      }
      return;
    }

    const request: POSTApplyPendingTransactionsRequest = { items };
    this.applying.set(true);
    this.applyFailures.set(clientFailures);

    this.pendingTransactions
      .apply(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const data = res.data;
          const success = data?.successCount ?? 0;
          const failure = data?.failureCount ?? 0;

          if (success > 0) {
            this.toast.showSuccess(`Converted ${success} pending transactions to journal entries.`);
          }
          if (failure > 0 || clientFailures.length > 0) {
            const combined: { label: string; message: string }[] = [
              ...clientFailures,
              ...this.mapBackendFailures(data?.failed ?? []),
            ];
            this.applyFailures.set(combined);
            this.toast.showError(`${failure} pending transactions failed to convert. See details below.`);
          }

          this.applying.set(false);
          this.refresh();
        },
        error: () => {
          this.applying.set(false);
          this.toast.showError('Apply failed.');
        },
      });
  }

  private draftHasAnyLineInfo(draft: IPendingDraft): boolean {
    const lines = draft.lines ?? [];
    if (lines.length > 2) return true;
    for (const l of lines) {
      if (l.accountId != null) return true;
      if ((l.notes ?? '').trim().length > 0) return true;
    }
    return false;
  }

  private validateDraftForTx(
    tx: PendingTransactionObject,
    draft: IPendingDraft,
  ): { valid: boolean; message: string | null } {
    const desc = (draft.description ?? '').trim();
    if (desc.length === 0) return { valid: false, message: 'Description is required.' };

    const base = validateJournalEntryDraft(draft.lines ?? []);
    if (!base.valid) return { valid: false, message: base.errorMessage };

    const debit = this.draftDebitTotalMinor(draft);
    const credit = this.draftCreditTotalMinor(draft);
    if (debit !== tx.amount || credit !== tx.amount) {
      return { valid: false, message: 'Debit and credit totals must equal the pending amount.' };
    }

    return { valid: true, message: null };
  }

  private draftDebitTotalMinor(draft: IPendingDraft): number {
    let sum = 0;
    for (const line of draft.lines ?? []) {
      if (line.direction !== 'D') continue;
      const m = dollarsStringToMinorUnits(line.amountDollars);
      if (m != null) sum += m;
    }
    return sum;
  }

  private draftCreditTotalMinor(draft: IPendingDraft): number {
    let sum = 0;
    for (const line of draft.lines ?? []) {
      if (line.direction !== 'C') continue;
      const m = dollarsStringToMinorUnits(line.amountDollars);
      if (m != null) sum += m;
    }
    return sum;
  }

  private failureLabelForTx(tx: PendingTransactionObject): string {
    return `${this.formatDate(tx.transactionDate)} — ${tx.description}`;
  }

  private mapBackendFailures(failed: ApplyPendingTransactionsFailureObject[]): { label: string; message: string }[] {
    const txs = this.txs();
    return failed.map((f) => {
      const tx = txs.find((t) => t.transactionNumber === f.pendingTransactionNumber);
      const label = tx ? this.failureLabelForTx(tx) : `#${f.pendingTransactionNumber}`;
      return { label, message: f.message };
    });
  }

  private formatDate(iso: string): string {
    if (!iso) return '';
    const d = new Date(iso + 'T12:00:00');
    return new Intl.DateTimeFormat(undefined, { year: 'numeric', month: 'short', day: 'numeric' }).format(d);
  }
}

