import {
  ILedgerFilterSortState,
  LedgerDateRangeOption,
  LedgerSortByOption,
} from '../../../domain/ui-preferences/ledger-display-settings';

export type { ILedgerFilterSortState, LedgerDateRangeOption, LedgerSortByOption };

export const DEFAULT_LEDGER_FILTER_STATE: ILedgerFilterSortState = {
  searchTerm: '',
  selectedDate: 'Last 30 days',
  selectedSortBy: 'Date (Asc.)',
  selectedAccountTypeIds: [],
  selectedAccountIds: [],
};

export function cloneLedgerFilterState(state: ILedgerFilterSortState): ILedgerFilterSortState {
  return {
    searchTerm: state.searchTerm,
    selectedDate: state.selectedDate,
    selectedSortBy: state.selectedSortBy,
    selectedAccountTypeIds: [...state.selectedAccountTypeIds],
    selectedAccountIds: [...state.selectedAccountIds],
  };
}
