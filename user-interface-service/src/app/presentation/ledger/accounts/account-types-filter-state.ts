import { AccountTypeObject } from '../../../adapter/ledger-service/dto/account-types/AccountTypeObject';
import {
  AccountTypesSortByOption,
  IAccountTypesFilterState,
} from '../../../domain/ui-preferences/account-types-display-settings';

export type { AccountTypesSortByOption, IAccountTypesFilterState };

/** Table row with resolved classification label for display. */
export type AccountTypeRowView = AccountTypeObject & { classificationLabel: string };

export const DEFAULT_ACCOUNT_TYPES_FILTER_STATE: IAccountTypesFilterState = {
  searchTerm: '',
  selectedClassificationIds: [],
  selectedSortBy: 'Description (Asc.)',
  showInactive: false,
  hideActiveOnly: false,
  hideSystemAccounts: false,
};

export function cloneAccountTypesFilterState(state: IAccountTypesFilterState): IAccountTypesFilterState {
  return {
    searchTerm: state.searchTerm,
    selectedClassificationIds: [...state.selectedClassificationIds],
    selectedSortBy: state.selectedSortBy,
    showInactive: state.showInactive,
    hideActiveOnly: state.hideActiveOnly,
    hideSystemAccounts: state.hideSystemAccounts,
  };
}
