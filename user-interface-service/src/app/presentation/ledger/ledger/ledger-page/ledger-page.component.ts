import { Component, DestroyRef, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
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

  ngOnInit(): void {
    this.applyFilters({ nextState: this.currentState(), markApplied: true });
  }

  onStateChanged(nextState: ILedgerFilterSortState): void {
    this.currentState.set(cloneFilterSortState(nextState));
  }

  get isDirtySinceLastApply(): boolean {
    return !this.statesEqual(this.currentState(), this.lastAppliedState());
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

  applyButtonClicked(): void {
    // #region agent log
    fetch('http://127.0.0.1:7699/ingest/2fb30966-6fce-4ce3-9190-7064cc5feee2',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'1c67cb'},body:JSON.stringify({sessionId:'1c67cb',runId:'pre-fix',hypothesisId:'H1',location:'ledger-page.component.ts:applyButtonClicked',message:'Apply/Refresh clicked',data:{isDirtySinceLastApply:this.isDirtySinceLastApply,currentState:this.currentState(),lastAppliedState:this.lastAppliedState()},timestamp:Date.now()})}).catch(()=>{});
    // #endregion
    if (this.isDirtySinceLastApply) {
      this.applyFilters({ nextState: this.currentState(), markApplied: true });
      return;
    }
    this.refresh();
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
    // #region agent log
    fetch('http://127.0.0.1:7699/ingest/2fb30966-6fce-4ce3-9190-7064cc5feee2',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'1c67cb'},body:JSON.stringify({sessionId:'1c67cb',runId:'pre-fix',hypothesisId:'H2',location:'ledger-page.component.ts:applyFilters',message:'Applying filters -> getAll(request)',data:{markApplied:opts.markApplied,nextState:opts.nextState,request},timestamp:Date.now()})}).catch(()=>{});
    // #endregion
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
          const journalEntryList: JournalEntryDTOEnrichedResponse[] = res.data?.journalEntryList ?? [];
          const entryDates: string[] = journalEntryList.map((e) => e.entryDate).filter((d) => !!d);
          const minDate: string | null = entryDates.length > 0 ? entryDates.reduce((a, b) => (a < b ? a : b)) : null;
          const maxDate: string | null = entryDates.length > 0 ? entryDates.reduce((a, b) => (a > b ? a : b)) : null;
          // #region agent log
          fetch('http://127.0.0.1:7699/ingest/2fb30966-6fce-4ce3-9190-7064cc5feee2',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'1c67cb'},body:JSON.stringify({sessionId:'1c67cb',runId:'pre-fix',hypothesisId:'H5',location:'ledger-page.component.ts:applyFilters:next',message:'getAll response received',data:{request,returnedCount:journalEntryList.length,minEntryDate:minDate,maxEntryDate:maxDate,sampleIds:journalEntryList.slice(0,5).map((e)=>e.id)},timestamp:Date.now()})}).catch(()=>{});
          // #endregion
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
        next: (res) => {
          // requested behavior
          // eslint-disable-next-line no-console
          console.log(res);
          this.removingEntryId.set(null);
          this.closeDeleteConfirm();
          this.refresh();
        },
        error: (err: unknown) => {
          // eslint-disable-next-line no-console
          console.error(err);
          this.removingEntryId.set(null);
          this.modalError.set('Could not remove that journal entry.');
        },
      });
  }

  onAddCreatedSuccessfully(res: unknown): void {
    // eslint-disable-next-line no-console
    console.log(res);
    this.closeAddModal();
    this.refresh();
  }

  onAddCancelled(): void {
    this.closeAddModal();
  }

  onEditUpdatedSuccessfully(res: unknown): void {
    // eslint-disable-next-line no-console
    console.log(res);
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
      filters.descriptionContains = trimmedSearch;
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
