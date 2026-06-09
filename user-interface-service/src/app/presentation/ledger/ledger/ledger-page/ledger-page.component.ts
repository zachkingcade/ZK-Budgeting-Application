import { Component, DestroyRef, OnInit, signal, ViewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, map, of, Subject } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { PageCage } from '../../../page-cage/page-cage.component';
import { LedgerTable } from '../ledger-table/ledger-table.component';
import { LedgerSortAndFilterBar } from '../ledger-sort-and-filter-bar/ledger-sort-and-filter-bar.component';
import { JournalEntryApplicationService } from '../../../../application/ledger/journal-entry.application-service';
import { JournalEntryDTOEnrichedResponse } from '../../../../adapter/ledger-service/dto/journal-entry/JournalEntryDTOEnrichedResponse';
import { GETAllJournalEntrysRequest } from '../../../../adapter/ledger-service/dto/journal-entry/GETAllJournalEntrysRequest';
import { JournalEntryFilters } from '../../../../adapter/ledger-service/dto/journal-entry/JournalEntryFilters';
import { SortObject } from '../../../../adapter/ledger-service/dto/SortObject';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { ToastService } from '../../../../application/toast.service';
import {
  SaveAsReplayableDialogComponent,
  SaveAsReplayableDialogData,
} from '../../pending/save-as-replayable-dialog.component';
import { enrichedJournalLinesToDrafts } from '../../../../domain/replayable-journal-entry/replayable-journal-entry.mapper';
import { ConfirmationModalComponent } from '../../../shared/confirmation-modal/confirmation-modal.component';
import { AddJournalEntryModalComponent } from '../modals/add-journal-entry-modal/add-journal-entry-modal.component';
import { EditJournalEntryModalComponent } from '../modals/edit-journal-entry-modal/edit-journal-entry-modal.component';
import { UserPreferencesService } from '../../../../application/settings/user-preferences.service';
import {
  cloneLedgerFilterState,
  DEFAULT_LEDGER_FILTER_STATE,
  ILedgerFilterSortState,
  LedgerDateRangeOption,
} from '../ledger-filter-state';

export type { ILedgerFilterSortState, LedgerDateRangeOption, LedgerSortByOption } from '../ledger-filter-state';

@Component({
  selector: 'app-ledger-page',
  standalone: true,
  imports: [
    PageCage,
    LedgerTable,
    LedgerSortAndFilterBar,
    MatButtonModule,
    MatIconModule,
    ConfirmationModalComponent,
    AddJournalEntryModalComponent,
    EditJournalEntryModalComponent,
  ],
  templateUrl: './ledger-page.component.html',
  styleUrl: './ledger-page.component.scss',
})
export class LedgerPage implements OnInit {
  @ViewChild(LedgerTable) private ledgerTable?: LedgerTable;

  constructor(
    private readonly journalEntries: JournalEntryApplicationService,
    private readonly dialog: MatDialog,
    private readonly toast: ToastService,
    private readonly userPreferences: UserPreferencesService,
    private readonly destroyRef: DestroyRef,
  ) {}

  readonly entries = signal<JournalEntryDTOEnrichedResponse[]>([]);
  readonly loading = signal<boolean>(false);
  readonly loadError = signal<string | null>(null);
  readonly removingEntryId = signal<number | null>(null);

  readonly currentState = signal<ILedgerFilterSortState>(cloneLedgerFilterState(DEFAULT_LEDGER_FILTER_STATE));
  readonly lastAppliedState = signal<ILedgerFilterSortState>(cloneLedgerFilterState(DEFAULT_LEDGER_FILTER_STATE));

  readonly addModalOpen = signal<boolean>(false);
  readonly editModalOpen = signal<boolean>(false);
  readonly deleteConfirmOpen = signal<boolean>(false);

  readonly modalError = signal<string | null>(null);

  readonly entryBeingEdited = signal<JournalEntryDTOEnrichedResponse | null>(null);
  readonly entryPendingDelete = signal<JournalEntryDTOEnrichedResponse | null>(null);

  private readonly loadRequest$ = new Subject<ILedgerFilterSortState>();

  ngOnInit(): void {
    this.loadRequest$
      .pipe(
        switchMap((state) => {
          const cloned = cloneLedgerFilterState(state);
          this.lastAppliedState.set(cloned);
          this.loading.set(true);
          this.loadError.set(null);
          const request = this.buildGetAllRequest(cloned);
          return this.journalEntries.getAll(request).pipe(
            map((res) => res.data?.journalEntryList ?? []),
            catchError(() => {
              this.loadError.set('Could not load journal entries.');
              return of([] as JournalEntryDTOEnrichedResponse[]);
            }),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((list) => {
        this.entries.set(list);
        this.loading.set(false);
      });

    this.initStateFromPreferences();
  }

  private initStateFromPreferences(): void {
    const apply = () => {
      const initial = cloneLedgerFilterState(this.userPreferences.getLedgerInitialState());
      this.currentState.set(initial);
      this.requestLoad(initial);
    };
    if (this.userPreferences.isHydrated()) {
      apply();
      return;
    }
    this.userPreferences.ensureLoaded().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => apply());
  }

  onStateChanged(nextState: ILedgerFilterSortState): void {
    const cloned = cloneLedgerFilterState(nextState);
    this.currentState.set(cloned);
    this.requestLoad(cloned);
  }

  showReturnToDefaults(): boolean {
    if (!this.userPreferences.hasLedgerDisplayDefaults()) {
      return false;
    }
    const initial = this.userPreferences.getLedgerInitialState();
    const current = this.currentState();
    return (
      current.selectedDate !== initial.selectedDate || current.selectedSortBy !== initial.selectedSortBy
    );
  }

  returnToDefaultsClicked(): void {
    const initial = cloneLedgerFilterState(this.userPreferences.getLedgerInitialState());
    this.currentState.set(initial);
    this.requestLoad(initial);
  }

  openAddModal(): void {
    this.modalError.set(null);
    this.addModalOpen.set(true);
  }

  closeAddModal(): void {
    this.addModalOpen.set(false);
  }

  openSaveAsReplayable(entry: JournalEntryDTOEnrichedResponse): void {
    const lines = entry.journalLines ?? [];
    if (lines.length === 0) {
      this.toast.showError('This journal entry has no lines to save.');
      return;
    }
    const data: SaveAsReplayableDialogData = {
      defaultName: entry.description?.trim() || `Journal entry ${entry.id}`,
      lines: enrichedJournalLinesToDrafts(lines),
      requireBalanced: true,
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

  openEditModal(entry: JournalEntryDTOEnrichedResponse): void {
    this.modalError.set(null);
    this.entryBeingEdited.set(entry);
    this.editModalOpen.set(true);
  }

  closeEditModal(): void {
    this.editModalOpen.set(false);
    this.entryBeingEdited.set(null);
  }

  openDeleteConfirm(entry: JournalEntryDTOEnrichedResponse): void {
    this.modalError.set(null);
    this.entryPendingDelete.set(entry);
    this.deleteConfirmOpen.set(true);
  }

  deleteConfirmMessage(): string {
    const entry: JournalEntryDTOEnrichedResponse | null = this.entryPendingDelete();
    if (!entry) {
      return '';
    }
    return `Remove journal entry "${entry.description}"?`;
  }

  closeDeleteConfirm(): void {
    this.deleteConfirmOpen.set(false);
    this.entryPendingDelete.set(null);
  }

  clearClicked(): void {
    const defaultState = cloneLedgerFilterState(DEFAULT_LEDGER_FILTER_STATE);
    this.currentState.set(defaultState);
    this.requestLoad(defaultState);
  }

  refresh(): void {
    this.requestLoad(this.lastAppliedState());
  }

  private requestLoad(state: ILedgerFilterSortState): void {
    this.ledgerTable?.collapseAllExpanded();
    this.loadRequest$.next(cloneLedgerFilterState(state));
  }

  onDeleteConfirmed(): void {
    const entry: JournalEntryDTOEnrichedResponse | null = this.entryPendingDelete();
    if (!entry) {
      this.closeDeleteConfirm();
      return;
    }

    this.removingEntryId.set(entry.id);
    this.modalError.set(null);

    this.journalEntries
      .removeById(entry.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.removingEntryId.set(null);
          this.closeDeleteConfirm();
          this.refresh();
        },
        error: () => {
          this.removingEntryId.set(null);
          this.modalError.set('Could not remove that journal entry.');
        },
      });
  }

  onAddCreatedSuccessfully(): void {
    this.closeAddModal();
    this.refresh();
  }

  onAddCancelled(): void {
    this.closeAddModal();
  }

  onEditUpdatedSuccessfully(): void {
    this.closeEditModal();
    this.refresh();
  }

  onEditCancelled(): void {
    this.closeEditModal();
  }

  private buildGetAllRequest(state: ILedgerFilterSortState): GETAllJournalEntrysRequest {
    const filters: JournalEntryFilters = {};
    const trimmedSearch: string = (state.searchTerm ?? '').trim();
    if (trimmedSearch.length > 0) {
      filters.searchContains = trimmedSearch;
    }
    if (state.selectedAccountTypeIds.length > 0) {
      filters.accountTypes = state.selectedAccountTypeIds;
    }
    if (state.selectedAccountIds.length > 0) {
      filters.accounts = state.selectedAccountIds;
    }

    const todayIso: string = this.toIsoDate(new Date());
    if (state.selectedDate !== 'All unarchived') {
      const daysBack: number = this.daysBackFromOption(state.selectedDate);
      const after: Date = new Date();
      after.setDate(after.getDate() - daysBack);
      filters.dateAfter = this.toIsoDate(after);
      filters.dateBefore = todayIso;
    }

    const sort: SortObject<'entryDate'> = {
      type: 'entryDate',
      direction: state.selectedSortBy === 'Date (Asc.)' ? 'ascending' : 'descending',
    };

    return { sort, filters };
  }

  private daysBackFromOption(option: LedgerDateRangeOption): number {
    if (option === 'Last 7 days') return 7;
    if (option === 'Last 14 days') return 14;
    if (option === 'Last 30 days') return 30;
    if (option === 'Last 60 days') return 60;
    return 30;
  }

  private toIsoDate(d: Date): string {
    const year: number = d.getFullYear();
    const month: string = String(d.getMonth() + 1).padStart(2, '0');
    const day: string = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
