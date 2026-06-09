import { Component, DestroyRef, EventEmitter, Input, OnInit, Output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MtxSelectModule } from '@ng-matero/extensions/select';
import { AccountTypesApplicationService } from '../../../../application/ledger/account-types.application-service';
import { catchError, of } from 'rxjs';
import {
  AccountsSortByOption,
  DEFAULT_ACCOUNTS_FILTER_STATE,
  IAccountsFilterState,
} from '../accounts-filter-state';

type SortOptionRow = { id: AccountsSortByOption; label: string };

@Component({
  selector: 'app-accounts-sort-and-filter-bar',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatSlideToggleModule,
    MtxSelectModule,
  ],
  templateUrl: './accounts-sort-and-filter-bar.component.html',
  styleUrl: './accounts-sort-and-filter-bar.component.scss',
})
export class AccountsSortAndFilterBarComponent implements OnInit {
  constructor(
    private readonly accountTypesApplicationService: AccountTypesApplicationService,
    private readonly destroyRef: DestroyRef,
  ) {}

  @Input() state: IAccountsFilterState = { ...DEFAULT_ACCOUNTS_FILTER_STATE };

  @Output() stateChanged = new EventEmitter<IAccountsFilterState>();

  readonly accountTypeOptions = signal<{ id: number; label: string }[]>([]);

  readonly sortByOptions: SortOptionRow[] = [
    { id: 'Description (Asc.)', label: 'Description (Asc.)' },
    { id: 'Description (Des.)', label: 'Description (Des.)' },
    { id: 'Creation order (Asc.)', label: 'Creation order (Asc.)' },
    { id: 'Creation order (Des.)', label: 'Creation order (Des.)' },
  ];

  ngOnInit(): void {
    this.accountTypesApplicationService
      .getAll()
      .pipe(catchError(() => of({ data: { accountTypeList: [] } } as any)))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const list: Array<{ id: number; description: string; active: boolean }> =
            (res.data?.accountTypeList ?? []) as any;
          const options = list.map((t: { id: number; description: string; active: boolean }) => ({
            id: t.id,
            label: t.active ? t.description : `${t.description} (inactive)`,
          }));
          this.accountTypeOptions.set(options);
          const valid = new Set(options.map((o) => o.id));
          const filtered = (this.state.selectedAccountTypeIds ?? []).filter((id) => valid.has(id));
          if (filtered.length !== (this.state.selectedAccountTypeIds ?? []).length) {
            this.emitSnapshot({ selectedAccountTypeIds: filtered });
          }
        },
      });
  }

  private emitSnapshot(overrides: Partial<IAccountsFilterState> = {}): void {
    this.stateChanged.emit({
      searchTerm: overrides.searchTerm ?? this.state.searchTerm,
      selectedAccountTypeIds: overrides.selectedAccountTypeIds ?? [...(this.state.selectedAccountTypeIds ?? [])],
      selectedSortBy: overrides.selectedSortBy ?? this.state.selectedSortBy,
      showInactive: overrides.showInactive ?? this.state.showInactive,
      hideActiveOnly: overrides.hideActiveOnly ?? this.state.hideActiveOnly,
      groupByAccountType: overrides.groupByAccountType ?? this.state.groupByAccountType,
    });
  }

  onSortByChange(selectedSortBy: AccountsSortByOption): void {
    this.emitSnapshot({ selectedSortBy });
  }

  onSearchChange(searchTerm: string | null): void {
    this.emitSnapshot({ searchTerm: searchTerm ?? '' });
  }

  onAccountTypesChange(value: unknown): void {
    this.emitSnapshot({ selectedAccountTypeIds: this.normalizeIdArray(value) });
  }

  private normalizeIdArray(value: unknown): number[] {
    if (value == null) {
      return [];
    }
    if (Array.isArray(value)) {
      return value
        .map((item) => this.extractId(item))
        .filter((id): id is number => id !== null);
    }
    const single = this.extractId(value);
    return single === null ? [] : [single];
  }

  private extractId(value: unknown): number | null {
    if (typeof value === 'number' && Number.isInteger(value)) {
      return value;
    }
    if (typeof value === 'object' && value !== null && 'id' in value) {
      const id = (value as { id: unknown }).id;
      if (typeof id === 'number' && Number.isInteger(id)) {
        return id;
      }
    }
    return null;
  }

  onShowInactiveChange(checked: boolean): void {
    this.emitSnapshot({
      showInactive: checked,
      hideActiveOnly: checked ? this.state.hideActiveOnly : false,
    });
  }

  onHideActiveOnlyChange(checked: boolean): void {
    this.emitSnapshot({
      hideActiveOnly: checked,
      showInactive: checked ? true : this.state.showInactive,
    });
  }

  onGroupByTypeChange(checked: boolean): void {
    this.emitSnapshot({ groupByAccountType: checked });
  }
}
