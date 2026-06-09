import { Component, DestroyRef, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, map, of, Subject } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { PageCage } from '../../../page-cage/page-cage.component';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AccountTypesApplicationService } from '../../../../application/ledger/account-types.application-service';
import { AccountClassificationsApplicationService } from '../../../../application/ledger/account-classifications.application-service';
import { AccountTypeFilters } from '../../../../adapter/ledger-service/dto/account-types/AccountTypeFilters';
import { GETAllAccountTypesRequest } from '../../../../adapter/ledger-service/dto/account-types/GETAllAccountTypesRequest';
import { AccountTypeObject } from '../../../../adapter/ledger-service/dto/account-types/AccountTypeObject';
import { SortObject } from '../../../../adapter/ledger-service/dto/SortObject';
import { ConfirmationModalComponent } from '../../../shared/confirmation-modal/confirmation-modal.component';
import { AccountTypesSortAndFilterBarComponent } from '../account-types-sort-and-filter-bar/account-types-sort-and-filter-bar.component';
import { AccountTypesTableComponent } from '../account-types-table/account-types-table.component';
import { AddAccountTypeModalComponent } from '../modals/add-account-type-modal/add-account-type-modal.component';
import { EditAccountTypeModalComponent } from '../modals/edit-account-type-modal/edit-account-type-modal.component';
import { UserPreferencesService } from '../../../../application/settings/user-preferences.service';
import {
  AccountTypeRowView,
  cloneAccountTypesFilterState,
  DEFAULT_ACCOUNT_TYPES_FILTER_STATE,
  IAccountTypesFilterState,
} from '../account-types-filter-state';

@Component({
  selector: 'app-account-types-page',
  standalone: true,
  imports: [
    PageCage,
    AccountTypesSortAndFilterBarComponent,
    AccountTypesTableComponent,
    MatButtonModule,
    MatIconModule,
    ConfirmationModalComponent,
    AddAccountTypeModalComponent,
    EditAccountTypeModalComponent,
  ],
  templateUrl: './account-types-page.component.html',
  styleUrl: './account-types-page.component.scss',
})
export class AccountTypesPageComponent implements OnInit {
  constructor(
    private readonly accountTypesApplicationService: AccountTypesApplicationService,
    private readonly accountClassificationsApplicationService: AccountClassificationsApplicationService,
    private readonly userPreferences: UserPreferencesService,
    private readonly destroyRef: DestroyRef,
  ) {}

  readonly accountTypes = signal<AccountTypeRowView[]>([]);
  readonly loading = signal(false);
  readonly loadError = signal<string | null>(null);

  readonly classificationLabelById = signal<Map<number, string>>(new Map());

  readonly currentState = signal<IAccountTypesFilterState>(
    cloneAccountTypesFilterState(DEFAULT_ACCOUNT_TYPES_FILTER_STATE),
  );
  readonly lastAppliedState = signal<IAccountTypesFilterState>(
    cloneAccountTypesFilterState(DEFAULT_ACCOUNT_TYPES_FILTER_STATE),
  );

  readonly addModalOpen = signal(false);
  readonly editModalOpen = signal(false);
  readonly toggleActiveConfirmOpen = signal(false);

  readonly modalError = signal<string | null>(null);
  readonly typeBeingEdited = signal<AccountTypeRowView | null>(null);
  readonly typePendingToggle = signal<AccountTypeRowView | null>(null);
  readonly togglingTypeId = signal<number | null>(null);

  private readonly loadRequest$ = new Subject<IAccountTypesFilterState>();

  ngOnInit(): void {
    this.loadRequest$
      .pipe(
        switchMap((state) => {
          const cloned = cloneAccountTypesFilterState(state);
          this.lastAppliedState.set(cloned);
          this.loading.set(true);
          this.loadError.set(null);
          return this.accountTypesApplicationService.getAll(this.buildGetAllRequest(cloned)).pipe(
            map((res) => {
              const list: AccountTypeObject[] = res.data?.accountTypeList ?? [];
              const labelMap = this.classificationLabelById();
              return list.map((t) => ({
                ...t,
                classificationLabel: labelMap.get(t.classificationId) ?? '—',
              }));
            }),
            catchError(() => {
              this.loadError.set('Could not load account types.');
              return of([] as AccountTypeRowView[]);
            }),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((rows) => {
        this.accountTypes.set(rows);
        this.loading.set(false);
      });

    this.accountClassificationsApplicationService
      .getAll()
      .pipe(
        catchError(() =>
          of({
            data: { accountClassificationList: [] },
          } as any),
        ),
      )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const list = res.data?.accountClassificationList ?? [];
          const map = new Map<number, string>();
          for (const c of list) {
            map.set(c.id, c.description);
          }
          this.classificationLabelById.set(map);
          this.initStateFromPreferences();
        },
      });
  }

  private initStateFromPreferences(): void {
    const apply = () => {
      const initial = cloneAccountTypesFilterState(this.userPreferences.getAccountTypesInitialState());
      this.currentState.set(initial);
      this.requestLoad(initial);
    };
    if (this.userPreferences.isHydrated()) {
      apply();
      return;
    }
    this.userPreferences.ensureLoaded().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => apply());
  }

  onStateChanged(nextState: IAccountTypesFilterState): void {
    const cloned = cloneAccountTypesFilterState(nextState);
    this.currentState.set(cloned);
    this.requestLoad(cloned);
  }

  showReturnToDefaults(): boolean {
    if (!this.userPreferences.hasAccountTypesDisplayDefaults()) {
      return false;
    }
    const initial = this.userPreferences.getAccountTypesInitialState();
    return this.currentState().selectedSortBy !== initial.selectedSortBy;
  }

  returnToDefaultsClicked(): void {
    const initial = cloneAccountTypesFilterState(this.userPreferences.getAccountTypesInitialState());
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

  openEditModal(row: AccountTypeRowView): void {
    this.modalError.set(null);
    this.typeBeingEdited.set(row);
    this.editModalOpen.set(true);
  }

  closeEditModal(): void {
    this.editModalOpen.set(false);
    this.typeBeingEdited.set(null);
  }

  openToggleActiveConfirm(row: AccountTypeRowView): void {
    this.modalError.set(null);
    this.typePendingToggle.set(row);
    this.toggleActiveConfirmOpen.set(true);
  }

  closeToggleActiveConfirm(): void {
    this.toggleActiveConfirmOpen.set(false);
    this.typePendingToggle.set(null);
  }

  toggleActiveConfirmHeader(): string {
    const t = this.typePendingToggle();
    if (!t) return 'Confirm';
    return t.active ? 'Disable account type' : 'Enable account type';
  }

  toggleActiveConfirmMessage(): string {
    const t = this.typePendingToggle();
    if (!t) return '';
    if (t.active) {
      return (
        `Disabling "${t.description}" prevents creating new accounts that use this account type. ` +
        `Existing accounts and historical data are not deleted, and this account type is not removed. ` +
        `You can re-enable it later.`
      );
    }
    return `Enable account type "${t.description}"?`;
  }

  toggleActiveConfirmLabel(): string {
    const t = this.typePendingToggle();
    if (!t) return 'Confirm';
    return t.active ? 'Disable' : 'Enable';
  }

  clearClicked(): void {
    const defaultState = cloneAccountTypesFilterState(DEFAULT_ACCOUNT_TYPES_FILTER_STATE);
    this.currentState.set(defaultState);
    this.requestLoad(defaultState);
  }

  refresh(): void {
    this.requestLoad(this.lastAppliedState());
  }

  private requestLoad(state: IAccountTypesFilterState): void {
    this.loadRequest$.next(cloneAccountTypesFilterState(state));
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
    const row = this.typePendingToggle();
    if (!row) {
      this.closeToggleActiveConfirm();
      return;
    }

    this.togglingTypeId.set(row.id);
    this.modalError.set(null);

    this.accountTypesApplicationService
      .update({ id: row.id, active: !row.active })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.togglingTypeId.set(null);
          this.closeToggleActiveConfirm();
          this.refresh();
        },
        error: () => {
          this.togglingTypeId.set(null);
          this.modalError.set('Could not update account type status.');
        },
      });
  }

  private buildGetAllRequest(state: IAccountTypesFilterState): GETAllAccountTypesRequest {
    const filters: AccountTypeFilters = {};
    const trimmedSearch = (state.searchTerm ?? '').trim();
    if (trimmedSearch.length > 0) {
      filters.searchContains = trimmedSearch;
    }
    if (state.selectedClassificationIds.length > 0) {
      filters.accountClass = state.selectedClassificationIds;
    }
    filters.hideInactive = !state.showInactive;
    filters.hideActive = state.hideActiveOnly;
    filters.hideSystemAccounts = state.hideSystemAccounts;

    const sort = this.buildSortObject(state.selectedSortBy);

    return { sort, filters };
  }

  private buildSortObject(
    selectedSortBy: IAccountTypesFilterState['selectedSortBy'],
  ): SortObject<'description' | 'id'> {
    const ascending = selectedSortBy.endsWith('(Asc.)');
    const direction = ascending ? 'ascending' : 'descending';
    if (selectedSortBy.startsWith('Creation order')) {
      return { type: 'id', direction };
    }
    return { type: 'description', direction };
  }
}
