import { Component, DestroyRef, EventEmitter, Input, OnInit, Output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MtxSelectModule } from '@ng-matero/extensions/select';
import { AccountClassificationsApplicationService } from '../../../../application/ledger/account-classifications.application-service';
import {
  AccountTypesSortByOption,
  DEFAULT_ACCOUNT_TYPES_FILTER_STATE,
  IAccountTypesFilterState,
} from '../account-types-filter-state';

type SortOptionRow = { id: AccountTypesSortByOption; label: string };

@Component({
  selector: 'app-account-types-sort-and-filter-bar',
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
  templateUrl: './account-types-sort-and-filter-bar.component.html',
  styleUrl: './account-types-sort-and-filter-bar.component.scss',
})
export class AccountTypesSortAndFilterBarComponent implements OnInit {
  constructor(
    private readonly accountClassificationsApplicationService: AccountClassificationsApplicationService,
    private readonly destroyRef: DestroyRef,
  ) {}

  @Input() state: IAccountTypesFilterState = { ...DEFAULT_ACCOUNT_TYPES_FILTER_STATE };

  @Output() stateChanged = new EventEmitter<IAccountTypesFilterState>();

  readonly classificationOptions = signal<{ id: number; label: string }[]>([]);

  readonly sortByOptions: SortOptionRow[] = [
    { id: 'Description (Asc.)', label: 'Description (Asc.)' },
    { id: 'Description (Des.)', label: 'Description (Des.)' },
    { id: 'Creation order (Asc.)', label: 'Creation order (Asc.)' },
    { id: 'Creation order (Des.)', label: 'Creation order (Des.)' },
  ];

  ngOnInit(): void {
    this.accountClassificationsApplicationService
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const list = res.data?.accountClassificationList ?? [];
          this.classificationOptions.set(list.map((c) => ({ id: c.id, label: c.description })));
        },
      });
  }

  private emitSnapshot(overrides: Partial<IAccountTypesFilterState> = {}): void {
    this.stateChanged.emit({
      searchTerm: overrides.searchTerm ?? this.state.searchTerm,
      selectedClassificationIds:
        overrides.selectedClassificationIds ?? [...(this.state.selectedClassificationIds ?? [])],
      selectedSortBy: overrides.selectedSortBy ?? this.state.selectedSortBy,
      showInactive: overrides.showInactive ?? this.state.showInactive,
      hideActiveOnly: overrides.hideActiveOnly ?? this.state.hideActiveOnly,
      hideSystemAccounts: overrides.hideSystemAccounts ?? this.state.hideSystemAccounts,
    });
  }

  onSortByChange(selectedSortBy: AccountTypesSortByOption): void {
    this.emitSnapshot({ selectedSortBy });
  }

  onSearchChange(searchTerm: string | null): void {
    this.emitSnapshot({ searchTerm: searchTerm ?? '' });
  }

  onClassificationsChange(value: unknown): void {
    this.emitSnapshot({ selectedClassificationIds: this.normalizeIdArray(value) });
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

  onHideSystemAccountsChange(checked: boolean): void {
    this.emitSnapshot({ hideSystemAccounts: checked });
  }
}
