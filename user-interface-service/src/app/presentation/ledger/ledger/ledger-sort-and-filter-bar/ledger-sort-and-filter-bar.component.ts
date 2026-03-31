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

  @Input() showRefresh: boolean = false;

  @Output() stateChanged: EventEmitter<ILedgerFilterSortState> = new EventEmitter<ILedgerFilterSortState>();
  @Output() clearClicked: EventEmitter<void> = new EventEmitter<void>();
  @Output() applyOrRefreshClicked: EventEmitter<void> = new EventEmitter<void>();

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
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const list = res.data?.accountTypeList ?? [];
          this.accountTypeOptions.set(
            list.map((t) => ({ id: t.id, label: t.active ? t.description : `${t.description} (inactive)` }))
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
              .map((a) => ({
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

  onClear(): void {
    // #region agent log
    fetch('http://127.0.0.1:7699/ingest/2fb30966-6fce-4ce3-9190-7064cc5feee2',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'1c67cb'},body:JSON.stringify({sessionId:'1c67cb',runId:'pre-fix',hypothesisId:'H3',location:'ledger-sort-and-filter-bar.component.ts:onClear',message:'Clear clicked',data:{state:this.state,showRefresh:this.showRefresh},timestamp:Date.now()})}).catch(()=>{});
    // #endregion
    this.clearClicked.emit();
  }

  onApplyOrRefresh(): void {
    // #region agent log
    fetch('http://127.0.0.1:7699/ingest/2fb30966-6fce-4ce3-9190-7064cc5feee2',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'1c67cb'},body:JSON.stringify({sessionId:'1c67cb',runId:'pre-fix',hypothesisId:'H3',location:'ledger-sort-and-filter-bar.component.ts:onApplyOrRefresh',message:'Apply/Refresh button clicked in bar',data:{state:this.state,showRefresh:this.showRefresh,label:this.showRefresh?'Refresh':'Apply'},timestamp:Date.now()})}).catch(()=>{});
    // #endregion
    this.applyOrRefreshClicked.emit();
  }
}
