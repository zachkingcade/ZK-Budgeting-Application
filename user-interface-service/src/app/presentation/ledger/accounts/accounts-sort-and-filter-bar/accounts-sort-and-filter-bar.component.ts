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

  @Input() showRefresh: boolean = false;

  @Output() stateChanged = new EventEmitter<IAccountsFilterState>();
  @Output() clearClicked = new EventEmitter<void>();
  @Output() applyOrRefreshClicked = new EventEmitter<void>();

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
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const list = res.data?.accountTypeList ?? [];
          this.accountTypeOptions.set(
            list.map((t) => ({
              id: t.id,
              label: t.active ? t.description : `${t.description} (inactive)`,
            })),
          );
        },
      });
  }

  emitState(): void {
    this.stateChanged.emit({
      searchTerm: this.state.searchTerm,
      selectedAccountTypeIds: [...this.state.selectedAccountTypeIds],
      selectedSortBy: this.state.selectedSortBy,
      showInactive: this.state.showInactive,
      hideActiveOnly: this.state.hideActiveOnly,
    });
  }

  onSortByChange(): void {
    this.emitState();
  }

  onSearchChange(): void {
    this.emitState();
  }

  onAccountTypesChange(): void {
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

  onClear(): void {
    this.clearClicked.emit();
  }

  onApplyOrRefresh(): void {
    this.applyOrRefreshClicked.emit();
  }
}
