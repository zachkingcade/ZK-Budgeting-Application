import { Component, DestroyRef, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { PageCage } from '../../../page-cage/page-cage.component';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AccountsApplicationService } from '../../../../application/ledger/accounts.application-service';
import { AccountEnrichedObject } from '../../../../adapter/ledger-service/dto/account/AccountEnrichedObject';
import { AccountFilterObject } from '../../../../adapter/ledger-service/dto/account/AccountFilterObject';
import { GETAllAccountsRequest } from '../../../../adapter/ledger-service/dto/account/GETAllAccountsRequest';
import { SortObject } from '../../../../adapter/ledger-service/dto/SortObject';
import { ConfirmationModalComponent } from '../../../shared/confirmation-modal/confirmation-modal.component';
import { AccountsSortAndFilterBarComponent } from '../accounts-sort-and-filter-bar/accounts-sort-and-filter-bar.component';
import { AccountsTableComponent } from '../accounts-table/accounts-table.component';
import { AddAccountModalComponent } from '../modals/add-account-modal/add-account-modal.component';
import { EditAccountModalComponent } from '../modals/edit-account-modal/edit-account-modal.component';
import {
  cloneAccountsFilterState,
  DEFAULT_ACCOUNTS_FILTER_STATE,
  IAccountsFilterState,
} from '../accounts-filter-state';

@Component({
  selector: 'app-accounts-page',
  standalone: true,
  imports: [
    PageCage,
    AccountsSortAndFilterBarComponent,
    AccountsTableComponent,
    MatButtonModule,
    MatIconModule,
    ConfirmationModalComponent,
    AddAccountModalComponent,
    EditAccountModalComponent,
  ],
  templateUrl: './accounts-page.component.html',
  styleUrl: './accounts-page.component.scss',
})
export class AccountsPageComponent implements OnInit {
  constructor(
    private readonly accountsApplicationService: AccountsApplicationService,
    private readonly destroyRef: DestroyRef,
  ) {}

  readonly accounts = signal<AccountEnrichedObject[]>([]);
  readonly loading = signal(false);
  readonly loadError = signal<string | null>(null);

  readonly currentState = signal<IAccountsFilterState>(cloneAccountsFilterState(DEFAULT_ACCOUNTS_FILTER_STATE));
  readonly lastAppliedState = signal<IAccountsFilterState>(
    cloneAccountsFilterState(DEFAULT_ACCOUNTS_FILTER_STATE),
  );

  readonly addModalOpen = signal(false);
  readonly editModalOpen = signal(false);
  readonly toggleActiveConfirmOpen = signal(false);
  /** Set when user tries to disable an active account whose balance is not zero. */
  readonly accountBlockedByBalance = signal<AccountEnrichedObject | null>(null);

  readonly modalError = signal<string | null>(null);
  readonly accountBeingEdited = signal<AccountEnrichedObject | null>(null);
  readonly accountPendingToggle = signal<AccountEnrichedObject | null>(null);
  readonly togglingAccountId = signal<number | null>(null);

  private readonly pendingFilterApply$ = new Subject<IAccountsFilterState>();

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

  onStateChanged(nextState: IAccountsFilterState): void {
    const cloned = cloneAccountsFilterState(nextState);
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

  openEditModal(account: AccountEnrichedObject): void {
    this.modalError.set(null);
    this.accountBeingEdited.set(account);
    this.editModalOpen.set(true);
  }

  closeEditModal(): void {
    this.editModalOpen.set(false);
    this.accountBeingEdited.set(null);
  }

  openToggleActiveConfirm(account: AccountEnrichedObject): void {
    this.modalError.set(null);
    if (account.active && account.accountBalance !== 0) {
      this.accountBlockedByBalance.set(account);
      return;
    }
    this.accountPendingToggle.set(account);
    this.toggleActiveConfirmOpen.set(true);
  }

  closeAccountBalanceBlockModal(): void {
    this.accountBlockedByBalance.set(null);
  }

  accountBalanceBlockMessage(): string {
    const acc = this.accountBlockedByBalance();
    if (!acc) {
      return '';
    }
    const current = this.formatMoneyMinor(acc.accountBalance);
    const zero = this.formatMoneyMinor(0);
    return (
      `The account balance must be ${zero} to disable an account. ` +
      `Current balance for "${acc.description}": ${current}.`
    );
  }

  private formatMoneyMinor(minorUnits: number): string {
    const major = minorUnits / 100;
    return new Intl.NumberFormat(undefined, { style: 'currency', currency: 'USD' }).format(major);
  }

  closeToggleActiveConfirm(): void {
    this.toggleActiveConfirmOpen.set(false);
    this.accountPendingToggle.set(null);
  }

  toggleActiveConfirmHeader(): string {
    const acc = this.accountPendingToggle();
    if (!acc) return 'Confirm';
    return acc.active ? 'Disable account' : 'Enable account';
  }

  toggleActiveConfirmMessage(): string {
    const acc = this.accountPendingToggle();
    if (!acc) return '';
    if (acc.active) {
      return (
        `Disabling "${acc.description}" prevents new journal entries from using this account. ` +
        `Existing journal history is not deleted. You can re-enable this account later.`
      );
    }
    return `Enable account "${acc.description}"?`;
  }

  toggleActiveConfirmLabel(): string {
    const acc = this.accountPendingToggle();
    if (!acc) return 'Confirm';
    return acc.active ? 'Disable' : 'Enable';
  }

  clearClicked(): void {
    const defaultState = cloneAccountsFilterState(DEFAULT_ACCOUNTS_FILTER_STATE);
    this.currentState.set(defaultState);
    this.applyFilters({ nextState: defaultState, markApplied: true });
  }

  refresh(): void {
    this.applyFilters({ nextState: this.lastAppliedState(), markApplied: false });
  }

  onAddCancelled(): void {
    this.closeAddModal();
  }

  onAddCreated(): void {
    this.closeAddModal();
    this.refresh();
  }

  onEditCancelled(): void {
    this.closeEditModal();
  }

  onEditUpdated(): void {
    this.closeEditModal();
    this.refresh();
  }

  onToggleActiveConfirmed(): void {
    const account = this.accountPendingToggle();
    if (!account) {
      this.closeToggleActiveConfirm();
      return;
    }

    this.togglingAccountId.set(account.accountId);
    this.modalError.set(null);

    this.accountsApplicationService
      .update({ id: account.accountId, active: !account.active })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.togglingAccountId.set(null);
          this.closeToggleActiveConfirm();
          this.refresh();
        },
        error: () => {
          this.togglingAccountId.set(null);
          this.modalError.set('Could not update account status.');
        },
      });
  }

  private applyFilters(opts: { nextState: IAccountsFilterState; markApplied: boolean }): void {
    if (opts.markApplied) {
      this.lastAppliedState.set(cloneAccountsFilterState(opts.nextState));
    }

    const request = this.buildGetAllRequest(opts.nextState);

    this.loading.set(true);
    this.loadError.set(null);

    this.accountsApplicationService
      .getAll(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.accounts.set(res.data?.accountsList ?? []);
          this.loading.set(false);
        },
        error: () => {
          this.loadError.set('Could not load accounts.');
          this.loading.set(false);
        },
      });
  }

  private buildGetAllRequest(state: IAccountsFilterState): GETAllAccountsRequest {
    const filters: AccountFilterObject = {};
    const trimmedSearch = (state.searchTerm ?? '').trim();
    if (trimmedSearch.length > 0) {
      filters.searchContains = trimmedSearch;
    }
    if (state.selectedAccountTypeIds.length > 0) {
      filters.accountTypes = state.selectedAccountTypeIds;
    }
    filters.hideInactive = !state.showInactive;
    filters.hideActive = state.hideActiveOnly;

    const sort = this.buildSortObject(state.selectedSortBy);

    return { sort, filters };
  }

  private buildSortObject(selectedSortBy: IAccountsFilterState['selectedSortBy']): SortObject<'description' | 'id'> {
    const ascending = selectedSortBy.endsWith('(Asc.)');
    const direction = ascending ? 'ascending' : 'descending';
    if (selectedSortBy.startsWith('Creation order')) {
      return { type: 'id', direction };
    }
    return { type: 'description', direction };
  }

  private statesEqual(a: IAccountsFilterState, b: IAccountsFilterState): boolean {
    return (
      a.searchTerm === b.searchTerm &&
      a.selectedSortBy === b.selectedSortBy &&
      a.showInactive === b.showInactive &&
      a.hideActiveOnly === b.hideActiveOnly &&
      this.arraysEqual(a.selectedAccountTypeIds, b.selectedAccountTypeIds)
    );
  }

  private arraysEqual(x: number[], y: number[]): boolean {
    if (x.length !== y.length) return false;
    const xs = [...x].sort((a, b) => a - b);
    const ys = [...y].sort((a, b) => a - b);
    return xs.every((v, i) => v === ys[i]);
  }
}
