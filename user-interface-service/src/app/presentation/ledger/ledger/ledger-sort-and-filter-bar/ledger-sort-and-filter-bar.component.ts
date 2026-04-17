import { Component, DestroyRef, EventEmitter, Input, Output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MtxSelectModule } from '@ng-matero/extensions/select';
import { AccountsApplicationService } from '../../../../application/ledger/accounts.application-service';
import { AccountTypesApplicationService } from '../../../../application/ledger/account-types.application-service';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { ILedgerFilterSortState, LedgerDateRangeOption, LedgerSortByOption } from '../ledger-page/ledger-page.component';
import { catchError, of } from 'rxjs';

type LedgerOption<TId extends string | number> = { id: TId; label: string };

@Component({
  selector: 'app-ledger-sort-and-filter-bar',
  standalone: true,
  imports: [CommonModule, FormsModule, MatFormFieldModule, MtxSelectModule, MatInputModule, MatIconModule],
  templateUrl: './ledger-sort-and-filter-bar.component.html',
  styleUrl: './ledger-sort-and-filter-bar.component.scss',
})
export class LedgerSortAndFilterBar {
  constructor(
    private readonly accountsApplicationService: AccountsApplicationService,
    private readonly accountTypesApplicationService: AccountTypesApplicationService,
    private readonly destroyRef: DestroyRef,
  ) {}

  @Input() state: ILedgerFilterSortState = {
    searchTerm: '',
    selectedDate: 'Last 30 days',
    selectedSortBy: 'Date (Asc.)',
    selectedAccountTypeIds: [],
    selectedAccountIds: [],
  };

  @Output() stateChanged: EventEmitter<ILedgerFilterSortState> = new EventEmitter<ILedgerFilterSortState>();

  readonly dateOptions: LedgerOption<LedgerDateRangeOption>[] = [
    { id: 'Last 30 days', label: 'Last 30 days' },
    { id: 'Last 14 days', label: 'Last 14 days' },
    { id: 'Last 7 days', label: 'Last 7 days' },
    { id: 'Last 60 days', label: 'Last 60 days' },
    { id: 'All unarchived', label: 'All unarchived' },
  ];
  readonly sortByOptions: LedgerOption<LedgerSortByOption>[] = [
    { id: 'Date (Asc.)', label: 'Date (Asc.)' },
    { id: 'Date (Des.)', label: 'Date (Des.)' },
  ];

  readonly accountTypeOptions = signal<{ id: number; label: string }[]>([]);
  readonly accountOptions = signal<{ id: number; label: string }[]>([]);

  ngOnInit(): void {
    this.accountTypesApplicationService
      .getAll()
      .pipe(catchError(() => of({ data: { accountTypeList: [] } } as any)))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const list: Array<{ id: number; description: string; active: boolean }> =
            (res.data?.accountTypeList ?? []) as any;
          this.accountTypeOptions.set(
            list.map((t: { id: number; description: string; active: boolean }) => ({
              id: t.id,
              label: t.active ? t.description : `${t.description} (inactive)`,
            }))
          );
        },
      });

    this.accountsApplicationService
      .getAll()
      .pipe(catchError(() => of({ data: { accountsList: [] } } as any)))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const list: Array<{ accountId: number; accountDisplayName: string; active: boolean }> =
            (res.data?.accountsList ?? []) as any;
          this.accountOptions.set(
            list
              .map((a: { accountId: number; accountDisplayName: string; active: boolean }) => ({
                id: a.accountId,
                label: a.active ? a.accountDisplayName : `${a.accountDisplayName} (inactive)`,
              }))
          );
        },
      });
  }

  onStateChanged(): void {
    const nextState: ILedgerFilterSortState = {
      searchTerm: this.state.searchTerm,
      selectedDate: this.state.selectedDate,
      selectedSortBy: this.state.selectedSortBy,
      selectedAccountTypeIds: [...this.state.selectedAccountTypeIds],
      selectedAccountIds: [...this.state.selectedAccountIds],
    };
    this.stateChanged.emit(nextState);
  }

}
