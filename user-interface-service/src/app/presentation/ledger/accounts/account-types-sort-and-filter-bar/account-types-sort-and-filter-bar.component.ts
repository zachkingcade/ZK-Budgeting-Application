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

  emitState(): void {
    this.stateChanged.emit({
      searchTerm: this.state.searchTerm,
      selectedClassificationIds: [...this.state.selectedClassificationIds],
      selectedSortBy: this.state.selectedSortBy,
      showInactive: this.state.showInactive,
      hideActiveOnly: this.state.hideActiveOnly,
      hideSystemAccounts: this.state.hideSystemAccounts,
    });
  }

  onSortByChange(): void {
    this.emitState();
  }

  onSearchChange(): void {
    this.emitState();
  }

  onClassificationsChange(): void {
    this.emitState();
  }

  onShowInactiveChange(): void {
    this.emitState();
  }

  onHideActiveOnlyChange(checked: boolean): void {
    if (checked && !this.state.showInactive) {
      this.state.showInactive = true;
    }
    this.emitState();
  }

  onHideSystemAccountsChange(checked: boolean): void {
    // Do not mutate this.state here: it is the same object as the parent's currentState and
    // mutating before emit makes the parent's hideToggled check see no change (prev already updated).
    this.stateChanged.emit({
      searchTerm: this.state.searchTerm,
      selectedClassificationIds: [...this.state.selectedClassificationIds],
      selectedSortBy: this.state.selectedSortBy,
      showInactive: this.state.showInactive,
      hideActiveOnly: this.state.hideActiveOnly,
      hideSystemAccounts: checked,
    });
  }
}
