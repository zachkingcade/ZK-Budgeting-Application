export type AccountsSortByOption =
  | 'Description (Asc.)'
  | 'Description (Des.)'
  | 'Creation order (Asc.)'
  | 'Creation order (Des.)';

export const ACCOUNTS_SORT_BY_OPTIONS: readonly AccountsSortByOption[] = [
  'Description (Asc.)',
  'Description (Des.)',
  'Creation order (Asc.)',
  'Creation order (Des.)',
] as const;

export const ACCOUNTS_DISPLAY_SETTING_KEYS = {
  accountTypeIds: 'accounts.default.account_type_ids',
  sortBy: 'accounts.default.sort_by',
} as const;

export interface AccountsDisplaySettingsForm {
  accountTypeIds: number[];
  sortBy: AccountsSortByOption | '';
}

export const EMPTY_ACCOUNTS_DISPLAY_SETTINGS: AccountsDisplaySettingsForm = {
  accountTypeIds: [],
  sortBy: '',
};

export interface IAccountsFilterState {
  searchTerm: string;
  selectedAccountTypeIds: number[];
  selectedSortBy: AccountsSortByOption;
  showInactive: boolean;
  hideActiveOnly: boolean;
  groupByAccountType: boolean;
}

export function isAccountsSortByOption(value: string): value is AccountsSortByOption {
  return (ACCOUNTS_SORT_BY_OPTIONS as readonly string[]).includes(value);
}

export function parseAccountTypeIdsJson(raw: string | undefined): number[] {
  if (!raw || raw.trim() === '') {
    return [];
  }
  try {
    const parsed = JSON.parse(raw) as unknown;
    if (!Array.isArray(parsed)) {
      return [];
    }
    return parsed.filter((id): id is number => typeof id === 'number' && Number.isInteger(id));
  } catch {
    return [];
  }
}

export function serializeAccountTypeIds(ids: number[]): string {
  if (ids.length === 0) {
    return '';
  }
  return JSON.stringify(ids);
}

export function mergeAccountsDisplayDefaults(
  saved: AccountsDisplaySettingsForm,
  pageDefault: IAccountsFilterState,
  validAccountTypeIds?: readonly number[],
): IAccountsFilterState {
  let typeIds = saved.accountTypeIds;
  if (validAccountTypeIds != null && typeIds.length > 0) {
    const valid = new Set(validAccountTypeIds);
    typeIds = typeIds.filter((id) => valid.has(id));
  }
  return {
    ...pageDefault,
    selectedAccountTypeIds: typeIds.length > 0 ? [...typeIds] : pageDefault.selectedAccountTypeIds,
    selectedSortBy:
      saved.sortBy && isAccountsSortByOption(saved.sortBy) ? saved.sortBy : pageDefault.selectedSortBy,
  };
}
