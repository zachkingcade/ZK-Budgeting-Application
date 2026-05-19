export type LedgerDateRangeOption =
  | 'Last 7 days'
  | 'Last 14 days'
  | 'Last 30 days'
  | 'Last 60 days'
  | 'All unarchived';

export type LedgerSortByOption = 'Date (Asc.)' | 'Date (Des.)';

export const LEDGER_DATE_RANGE_OPTIONS: readonly LedgerDateRangeOption[] = [
  'Last 7 days',
  'Last 14 days',
  'Last 30 days',
  'Last 60 days',
  'All unarchived',
] as const;

export const LEDGER_SORT_BY_OPTIONS: readonly LedgerSortByOption[] = ['Date (Asc.)', 'Date (Des.)'] as const;

export const LEDGER_DISPLAY_SETTING_KEYS = {
  dateRange: 'ledger.default.date_range',
  sortBy: 'ledger.default.sort_by',
} as const;

export interface LedgerDisplaySettingsForm {
  dateRange: LedgerDateRangeOption | '';
  sortBy: LedgerSortByOption | '';
}

export const EMPTY_LEDGER_DISPLAY_SETTINGS: LedgerDisplaySettingsForm = {
  dateRange: '',
  sortBy: '',
};

export interface ILedgerFilterSortState {
  searchTerm: string;
  selectedDate: LedgerDateRangeOption;
  selectedSortBy: LedgerSortByOption;
  selectedAccountTypeIds: number[];
  selectedAccountIds: number[];
}

export function isLedgerDateRangeOption(value: string): value is LedgerDateRangeOption {
  return (LEDGER_DATE_RANGE_OPTIONS as readonly string[]).includes(value);
}

export function isLedgerSortByOption(value: string): value is LedgerSortByOption {
  return (LEDGER_SORT_BY_OPTIONS as readonly string[]).includes(value);
}

export function mergeLedgerDisplayDefaults(
  saved: LedgerDisplaySettingsForm,
  pageDefault: ILedgerFilterSortState,
): ILedgerFilterSortState {
  return {
    ...pageDefault,
    selectedDate:
      saved.dateRange && isLedgerDateRangeOption(saved.dateRange) ? saved.dateRange : pageDefault.selectedDate,
    selectedSortBy:
      saved.sortBy && isLedgerSortByOption(saved.sortBy) ? saved.sortBy : pageDefault.selectedSortBy,
  };
}
