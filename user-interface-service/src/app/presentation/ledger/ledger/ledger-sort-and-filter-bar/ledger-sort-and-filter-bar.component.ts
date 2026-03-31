import { Component, DestroyRef, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MtxSelectModule } from '@ng-matero/extensions/select';
import { AccountsApplicationService } from '../../../../application/ledger/accounts.application-service';
import { AccountTypesApplicationService } from '../../../../application/ledger/account-types.application-service';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';

type LedgerOption<TId extends string | number> = { id: TId; label: string };

@Component({
  selector: 'app-ledger-sort-and-filter-bar',
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

  readonly dateOptions: LedgerOption<string>[] = [
    { id: 'Last 30 days', label: 'Last 30 days' },
    { id: 'Last 14 days', label: 'Last 14 days' },
    { id: 'Last 7 days', label: 'Last 7 days' },
    { id: 'Last 60 days', label: 'Last 60 days' },
    { id: 'All unarchived', label: 'All unarchived' },
  ];
  readonly sortByOptions: LedgerOption<string>[] = [
    { id: 'Date (Asc.)', label: 'Date (Asc.)' },
    { id: 'Date (Des.)', label: 'Date (Des.)' },
  ];

  readonly accountTypeOptions = signal<{ id: number; label: string }[]>([]);
  readonly accountOptions = signal<{ id: number; label: string }[]>([]);

  selectedAccountTypes: number[] = [];
  selectedAccounts: number[] = [];
  selectedDate: string = 'Last 30 days';
  selectedSortBy: string = 'Date (Asc.)';
  searchTerm: string = '';

  ngOnInit(): void {
    this.accountTypesApplicationService
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const list = res.data?.accountTypeList ?? [];
          this.accountTypeOptions.set(
            list.filter((t) => t.active).map((t) => ({ id: t.id, label: t.description }))
          );
        },
      });

    this.accountsApplicationService
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const list = res.data?.accountsList ?? [];
          this.accountOptions.set(
            list
              .filter((a) => a.active)
              .map((a) => ({ id: a.accountId, label: a.accountDisplayName }))
          );
        },
      });
  }
}
