import { Component, DestroyRef, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { PageCage } from '../../../page-cage/page-cage.component';
import { LedgerTable } from '../ledger-table/ledger-table.component';
import { LedgerSortAndFilterBar } from '../ledger-sort-and-filter-bar/ledger-sort-and-filter-bar.component';
import { JournalEntryApplicationService } from '../../../../application/ledger/journal-entry.application-service';
import { JournalEntryDTOEnrichedResponse } from '../../../../adapter/ledger-service/dto/journal-entry/JournalEntryDTOEnrichedResponse';
import { GETAllJournalEntrysRequest } from '../../../../adapter/ledger-service/dto/journal-entry/GETAllJournalEntrysRequest';
import { JournalEntryFilters } from '../../../../adapter/ledger-service/dto/journal-entry/JournalEntryFilters';
import { SortObject } from '../../../../adapter/ledger-service/dto/SortObject';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ConfirmationModalComponent } from '../../../shared/confirmation-modal/confirmation-modal.component';
import { AddJournalEntryModalComponent } from '../modals/add-journal-entry-modal/add-journal-entry-modal.component';
import { EditJournalEntryModalComponent } from '../modals/edit-journal-entry-modal/edit-journal-entry-modal.component';

export type LedgerDateRangeOption =
  | 'Last 7 days'
  | 'Last 14 days'
  | 'Last 30 days'
  | 'Last 60 days'
  | 'All unarchived';

export type LedgerSortByOption = 'Date (Asc.)' | 'Date (Des.)';

export interface ILedgerFilterSortState {
  searchTerm: string;
  selectedDate: LedgerDateRangeOption;
  selectedSortBy: LedgerSortByOption;
  selectedAccountTypeIds: number[];
  selectedAccountIds: number[];
}

const DEFAULT_FILTER_SORT_STATE: ILedgerFilterSortState = {
  searchTerm: '',
  selectedDate: 'Last 30 days',
  selectedSortBy: 'Date (Asc.)',
  selectedAccountTypeIds: [],
  selectedAccountIds: [],
};

function cloneFilterSortState(state: ILedgerFilterSortState): ILedgerFilterSortState {
  return {
    searchTerm: state.searchTerm,
    selectedDate: state.selectedDate,
    selectedSortBy: state.selectedSortBy,
    selectedAccountTypeIds: [...state.selectedAccountTypeIds],
    selectedAccountIds: [...state.selectedAccountIds],
  };
}

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
  constructor(
    private readonly journalEntries: JournalEntryApplicationService,
    private readonly destroyRef: DestroyRef,
  ) {}

  readonly entries = signal<JournalEntryDTOEnrichedResponse[]>([]);
  readonly loading = signal<boolean>(false);
  readonly loadError = signal<string | null>(null);
  readonly removingEntryId = signal<number | null>(null);

  readonly currentState = signal<ILedgerFilterSortState>(cloneFilterSortState(DEFAULT_FILTER_SORT_STATE));
  readonly lastAppliedState = signal<ILedgerFilterSortState>(cloneFilterSortState(DEFAULT_FILTER_SORT_STATE));

  readonly addModalOpen = signal<boolean>(false);
  readonly editModalOpen = signal<boolean>(false);
  readonly deleteConfirmOpen = signal<boolean>(false);

  readonly modalError = signal<string | null>(null);

  readonly entryBeingEdited = signal<JournalEntryDTOEnrichedResponse | null>(null);
  readonly entryPendingDelete = signal<JournalEntryDTOEnrichedResponse | null>(null);

  private readonly pendingFilterApply$ = new Subject<ILedgerFilterSortState>();

  ngOnInit(): void {
    this.pendingFilterApply$
      .pipe(
        debounceTime(1000),
        distinctUntilChanged((a, b) => this.statesEqual(a, b)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((state) => {
        if (!this.statesEqual(state, this.currentState())) {
          return;
        }
        this.applyFilters({ nextState: state, markApplied: true });
      });

    this.applyFilters({ nextState: this.currentState(), markApplied: true });
  }

  onStateChanged(nextState: ILedgerFilterSortState): void {
    const cloned = cloneFilterSortState(nextState);
    this.currentState.set(cloned);
    this.pendingFilterApply$.next(cloned);
  }

  openAddModal(): void {
    this.modalError.set(null);
    this.addModalOpen.set(true);
  }

  closeAddModal(): void {
    this.addModalOpen.set(false);
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
    const defaultState: ILedgerFilterSortState = cloneFilterSortState(DEFAULT_FILTER_SORT_STATE);
    this.currentState.set(defaultState);
    this.applyFilters({ nextState: defaultState, markApplied: true });
  }

  refresh(): void {
    this.applyFilters({ nextState: this.lastAppliedState(), markApplied: false });
  }

  private applyFilters(opts: { nextState: ILedgerFilterSortState; markApplied: boolean }): void {
    const request: GETAllJournalEntrysRequest = this.buildGetAllRequest(opts.nextState);
    if (opts.markApplied) {
      this.lastAppliedState.set(cloneFilterSortState(opts.nextState));
    }

    this.loading.set(true);
    this.loadError.set(null);
    this.journalEntries
      .getAll(request)
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

    const request: GETAllJournalEntrysRequest = {
      sort,
      filters,
    };
    return request;
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

  private statesEqual(a: ILedgerFilterSortState, b: ILedgerFilterSortState): boolean {
    return (
      a.searchTerm === b.searchTerm &&
      a.selectedDate === b.selectedDate &&
      a.selectedSortBy === b.selectedSortBy &&
      this.arraysEqual(a.selectedAccountTypeIds, b.selectedAccountTypeIds) &&
      this.arraysEqual(a.selectedAccountIds, b.selectedAccountIds)
    );
  }

  private arraysEqual(a: number[], b: number[]): boolean {
    if (a.length !== b.length) return false;
    const aSorted: number[] = [...a].sort((x, y) => x - y);
    const bSorted: number[] = [...b].sort((x, y) => x - y);
    for (let i: number = 0; i < aSorted.length; i++) {
      if (aSorted[i] !== bSorted[i]) return false;
    }
    return true;
  }
}
