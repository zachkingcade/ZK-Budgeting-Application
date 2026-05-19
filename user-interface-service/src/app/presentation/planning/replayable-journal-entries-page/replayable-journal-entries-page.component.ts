import { DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MtxSelectModule } from '@ng-matero/extensions/select';
import { PageCage } from '../../page-cage/page-cage.component';
import { ConfirmationModalComponent } from '../../shared/confirmation-modal/confirmation-modal.component';
import { TableRowActionsMenuComponent } from '../../shared/table-row-actions-menu/table-row-actions-menu.component';
import { ITableRowAction } from '../../shared/table-row-actions-menu/table-row-action.types';
import { ReplayableJournalEntryApplicationService } from '../../../application/planning/replayable-journal-entry.application-service';
import { AccountsApplicationService } from '../../../application/ledger/accounts.application-service';
import { ToastService } from '../../../application/toast.service';
import { readLedgerApiErrorMessage } from '../../../adapter/ledger-service/ledger-http-error.util';
import {
  IReplayableJournalEntryDetail,
  IReplayableJournalEntryListItem,
} from '../../../adapter/ledger-service/dto/replayable-journal-entry/replayable-journal-entry.dto';
import { AccountEnrichedObject } from '../../../adapter/ledger-service/dto/account/AccountEnrichedObject';
import {
  draftsToReplayableLines,
  replayableDetailToDrafts,
} from '../../../domain/replayable-journal-entry/replayable-journal-entry.mapper';
import {
  dollarsStringToMinorUnits,
  formatMoneyDollarsOnBlur,
  IJournalEntryLineDraft,
  onMoneyDollarsKeydown,
  sanitizeMoneyDollarsInput,
  validateJournalEntryDraft,
} from '../../../domain/journal-entry/journal-entry.validation';

type SelectOption<T extends string | number> = { id: T; label: string };

@Component({
  selector: 'app-replayable-journal-entries-page',
  standalone: true,
  imports: [
    PageCage,
    DatePipe,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MtxSelectModule,
    TableRowActionsMenuComponent,
    ConfirmationModalComponent,
  ],
  templateUrl: './replayable-journal-entries-page.component.html',
  styleUrl: './replayable-journal-entries-page.component.scss',
})
export class ReplayableJournalEntriesPageComponent implements OnInit {
  readonly items = signal<IReplayableJournalEntryListItem[]>([]);
  readonly listLoading = signal(false);
  readonly listError = signal<string | null>(null);
  readonly actionError = signal<string | null>(null);
  readonly saving = signal(false);

  readonly editorId = signal<number | 'new' | null>(null);
  readonly editorName = signal('');
  readonly editorLines = signal<IJournalEntryLineDraft[]>([]);
  readonly editorLoading = signal(false);

  readonly deleteConfirmOpen = signal(false);
  readonly deleteTarget = signal<IReplayableJournalEntryListItem | null>(null);
  readonly modalError = signal<string | null>(null);

  readonly accountOptions = signal<SelectOption<number>[]>([]);
  private readonly accountsById = signal<Map<number, AccountEnrichedObject>>(new Map());

  readonly listColumns = ['replayName', 'replayLineCount', 'lastEditedAt', 'actions'] as const;
  readonly directionOptions: SelectOption<'C' | 'D'>[] = [
    { id: 'D', label: 'Debit' },
    { id: 'C', label: 'Credit' },
  ];

  readonly inEditor = computed(() => this.editorId() != null);

  readonly debitTotalMinor = computed(() => this.sumDirection('D'));
  readonly creditTotalMinor = computed(() => this.sumDirection('C'));

  constructor(
    private readonly replayableService: ReplayableJournalEntryApplicationService,
    private readonly accounts: AccountsApplicationService,
    private readonly toast: ToastService,
    private readonly destroyRef: DestroyRef,
  ) {}

  ngOnInit(): void {
    this.loadAccounts();
    this.refreshList();
  }

  refreshList(): void {
    this.listLoading.set(true);
    this.listError.set(null);
    this.replayableService
      .list()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (list) => {
          this.items.set(list);
          this.listLoading.set(false);
        },
        error: (err: unknown) => {
          this.listError.set(readLedgerApiErrorMessage(err, 'Could not load replayable journal entries.'));
          this.listLoading.set(false);
        },
      });
  }

  openNew(): void {
    this.actionError.set(null);
    this.editorId.set('new');
    this.editorName.set('');
    this.editorLines.set([
      { amountDollars: '', accountId: null, direction: 'D', notes: '' },
      { amountDollars: '', accountId: null, direction: 'C', notes: '' },
    ]);
  }

  openEdit(item: IReplayableJournalEntryListItem): void {
    this.actionError.set(null);
    this.editorId.set(item.replayableJournalEntryId);
    this.editorLoading.set(true);
    this.replayableService
      .getById(item.replayableJournalEntryId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (detail: IReplayableJournalEntryDetail) => {
          this.editorName.set(detail.replayName);
          this.editorLines.set(replayableDetailToDrafts(detail));
          this.editorLoading.set(false);
        },
        error: (err: unknown) => {
          this.actionError.set(readLedgerApiErrorMessage(err, 'Could not open replayable journal entry.'));
          this.editorId.set(null);
          this.editorLoading.set(false);
        },
      });
  }

  closeEditor(): void {
    this.editorId.set(null);
    this.actionError.set(null);
  }

  saveEditor(): void {
    const name = this.editorName().trim();
    if (!name) {
      this.actionError.set('Replay name is required.');
      return;
    }
    const validation = validateJournalEntryDraft(this.editorLines());
    if (!validation.valid) {
      this.actionError.set(validation.errorMessage ?? 'Journal lines are incomplete or invalid.');
      return;
    }
    const lines = draftsToReplayableLines(this.editorLines());
    if (lines.length < 2) {
      this.actionError.set('At least two complete lines are required.');
      return;
    }
    const id = this.editorId();
    this.saving.set(true);
    this.actionError.set(null);
    const req$ =
      id === 'new'
        ? this.replayableService.create({ replayName: name, lines })
        : this.replayableService.update(id as number, { replayName: name, lines });
    req$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.saving.set(false);
        this.toast.showSuccess(id === 'new' ? 'Replayable journal entry created.' : 'Replayable journal entry saved.');
        this.closeEditor();
        this.refreshList();
      },
      error: (err: unknown) => {
        this.saving.set(false);
        this.actionError.set(readLedgerApiErrorMessage(err, 'Could not save replayable journal entry.'));
      },
    });
  }

  buildRowActions(_row: IReplayableJournalEntryListItem): ITableRowAction[] {
    return [
      { id: 'edit', label: 'Edit', icon: 'edit' },
      { id: 'delete', label: 'Delete', icon: 'delete' },
    ];
  }

  onRowAction(selection: { id: string }, row: IReplayableJournalEntryListItem): void {
    if (selection.id === 'edit') {
      this.openEdit(row);
    } else if (selection.id === 'delete') {
      this.deleteTarget.set(row);
      this.modalError.set(null);
      this.deleteConfirmOpen.set(true);
    }
  }

  deleteConfirmMessage(): string {
    const row = this.deleteTarget();
    return row ? `Delete replayable journal entry "${row.replayName}"?` : '';
  }

  closeDeleteConfirm(): void {
    this.deleteConfirmOpen.set(false);
    this.deleteTarget.set(null);
    this.modalError.set(null);
  }

  onDeleteConfirmed(): void {
    const row = this.deleteTarget();
    if (!row) {
      this.closeDeleteConfirm();
      return;
    }
    this.replayableService
      .delete(row.replayableJournalEntryId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toast.showSuccess('Replayable journal entry deleted.');
          this.closeDeleteConfirm();
          this.refreshList();
        },
        error: () => {
          this.modalError.set('Could not delete replayable journal entry.');
        },
      });
  }

  addLineRow(): void {
    this.editorLines.update((lines) => [...lines, { amountDollars: '', accountId: null, direction: '', notes: '' }]);
  }

  removeLineRow(index: number): void {
    this.editorLines.update((lines) => {
      if (lines.length <= 2) {
        return lines;
      }
      return lines.filter((_l, i) => i !== index);
    });
  }

  onLineAmountInput(index: number, ev: Event): void {
    const t = ev.target;
    if (!(t instanceof HTMLInputElement)) {
      return;
    }
    this.patchLine(index, { amountDollars: sanitizeMoneyDollarsInput(t.value) });
  }

  onLineAmountBlur(index: number): void {
    const line = this.editorLines()[index];
    if (!line) {
      return;
    }
    this.patchLine(index, { amountDollars: formatMoneyDollarsOnBlur(line.amountDollars) });
  }

  onMoneyKeydown(ev: KeyboardEvent): void {
    onMoneyDollarsKeydown(ev);
  }

  formatMoneyMinor(minor: number): string {
    return new Intl.NumberFormat(undefined, { style: 'currency', currency: 'USD' }).format(minor / 100);
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
    return `${line.direction === 'C' ? 'Credit' : 'Debit'} (${sign})`;
  }

  protected patchLine(index: number, patch: Partial<IJournalEntryLineDraft>): void {
    this.editorLines.update((lines) => lines.map((l, i) => (i === index ? { ...l, ...patch } : l)));
  }

  private sumDirection(direction: 'C' | 'D'): number {
    let sum = 0;
    for (const line of this.editorLines()) {
      if (line.direction !== direction) {
        continue;
      }
      const m = dollarsStringToMinorUnits(line.amountDollars);
      if (m != null) {
        sum += m;
      }
    }
    return sum;
  }

  private loadAccounts(): void {
    this.accounts
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const list: AccountEnrichedObject[] = res.data?.accountsList ?? [];
          const map = new Map<number, AccountEnrichedObject>();
          for (const a of list) {
            map.set(a.accountId, a);
          }
          this.accountsById.set(map);
          this.accountOptions.set(
            list.filter((a) => a.active).map((a) => ({ id: a.accountId, label: a.accountDisplayName })),
          );
        },
        error: () => {
          this.accountOptions.set([]);
          this.accountsById.set(new Map());
        },
      });
  }
}
