export type AccountsSortByOption =
  | 'Description (Asc.)'
  | 'Description (Des.)'
  | 'Creation order (Asc.)'
  | 'Creation order (Des.)';

export interface IAccountsFilterState {
  searchTerm: string;
  selectedAccountTypeIds: number[];
  selectedSortBy: AccountsSortByOption;
  /** When true, include inactive accounts in results (maps to hideInactive=false). */
  showInactive: boolean;
  /** When true, show only inactive accounts (maps to hideActive=true). */
  hideActiveOnly: boolean;
}

export const DEFAULT_ACCOUNTS_FILTER_STATE: IAccountsFilterState = {
  searchTerm: '',
  selectedAccountTypeIds: [],
  selectedSortBy: 'Description (Asc.)',
  showInactive: false,
  hideActiveOnly: false,
};

export function cloneAccountsFilterState(state: IAccountsFilterState): IAccountsFilterState {
  return {
    searchTerm: state.searchTerm,
    selectedAccountTypeIds: [...state.selectedAccountTypeIds],
    selectedSortBy: state.selectedSortBy,
    showInactive: state.showInactive,
    hideActiveOnly: state.hideActiveOnly,
  };
}
