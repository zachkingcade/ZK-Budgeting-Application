export type AccountTypesSortByOption =
  | 'Description (Asc.)'
  | 'Description (Des.)'
  | 'Creation order (Asc.)'
  | 'Creation order (Des.)';

export const ACCOUNT_TYPES_SORT_BY_OPTIONS: readonly AccountTypesSortByOption[] = [
  'Description (Asc.)',
  'Description (Des.)',
  'Creation order (Asc.)',
  'Creation order (Des.)',
] as const;

export const ACCOUNT_TYPES_DISPLAY_SETTING_KEYS = {
  sortBy: 'account_types.default.sort_by',
} as const;

export interface AccountTypesDisplaySettingsForm {
  sortBy: AccountTypesSortByOption | '';
}

export const EMPTY_ACCOUNT_TYPES_DISPLAY_SETTINGS: AccountTypesDisplaySettingsForm = {
  sortBy: '',
};

export interface IAccountTypesFilterState {
  searchTerm: string;
  selectedClassificationIds: number[];
  selectedSortBy: AccountTypesSortByOption;
  showInactive: boolean;
  hideActiveOnly: boolean;
  hideSystemAccounts: boolean;
}

export function isAccountTypesSortByOption(value: string): value is AccountTypesSortByOption {
  return (ACCOUNT_TYPES_SORT_BY_OPTIONS as readonly string[]).includes(value);
}

export function mergeAccountTypesDisplayDefaults(
  saved: AccountTypesDisplaySettingsForm,
  pageDefault: IAccountTypesFilterState,
): IAccountTypesFilterState {
  return {
    ...pageDefault,
    selectedSortBy:
      saved.sortBy && isAccountTypesSortByOption(saved.sortBy) ? saved.sortBy : pageDefault.selectedSortBy,
  };
}
