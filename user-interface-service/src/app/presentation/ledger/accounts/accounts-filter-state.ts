import {
  AccountsSortByOption,
  IAccountsFilterState,
} from '../../../domain/ui-preferences/accounts-display-settings';

export type { AccountsSortByOption, IAccountsFilterState };

export const DEFAULT_ACCOUNTS_FILTER_STATE: IAccountsFilterState = {
  searchTerm: '',
  selectedAccountTypeIds: [],
  selectedSortBy: 'Creation order (Asc.)',
  showInactive: false,
  hideActiveOnly: false,
  groupByAccountType: false,
};

export function cloneAccountsFilterState(state: IAccountsFilterState): IAccountsFilterState {
  return {
    searchTerm: state.searchTerm,
    selectedAccountTypeIds: [...state.selectedAccountTypeIds],
    selectedSortBy: state.selectedSortBy,
    showInactive: state.showInactive,
    hideActiveOnly: state.hideActiveOnly,
    groupByAccountType: state.groupByAccountType,
  };
}
