import { mergeAccountTypesDisplayDefaults } from './account-types-display-settings';
import { mergeAccountsDisplayDefaults } from './accounts-display-settings';
import { mergeLedgerDisplayDefaults } from './ledger-display-settings';
import { resolveUiThemeId } from './ui-theme-settings';

describe('ui preferences merge helpers', () => {
  const ledgerDefault = {
    searchTerm: '',
    selectedDate: 'Last 30 days' as const,
    selectedSortBy: 'Date (Asc.)' as const,
    selectedAccountTypeIds: [],
    selectedAccountIds: [],
  };

  it('mergeLedgerDisplayDefaults uses page default when saved empty', () => {
    const merged = mergeLedgerDisplayDefaults({ dateRange: '', sortBy: '' }, ledgerDefault);
    expect(merged.selectedDate).toBe('Last 30 days');
    expect(merged.selectedSortBy).toBe('Date (Asc.)');
  });

  it('mergeLedgerDisplayDefaults applies saved values', () => {
    const merged = mergeLedgerDisplayDefaults(
      { dateRange: 'Last 7 days', sortBy: 'Date (Des.)' },
      ledgerDefault,
    );
    expect(merged.selectedDate).toBe('Last 7 days');
    expect(merged.selectedSortBy).toBe('Date (Des.)');
  });

  it('mergeAccountsDisplayDefaults filters stale account type ids', () => {
    const pageDefault = {
      searchTerm: '',
      selectedAccountTypeIds: [],
      selectedSortBy: 'Creation order (Asc.)' as const,
      showInactive: false,
      hideActiveOnly: false,
      groupByAccountType: false,
    };
    const merged = mergeAccountsDisplayDefaults(
      { accountTypeIds: [1, 99], sortBy: '' },
      pageDefault,
      [1, 2],
    );
    expect(merged.selectedAccountTypeIds).toEqual([1]);
  });

  it('resolveUiThemeId falls back for invalid', () => {
    expect(resolveUiThemeId('not-a-theme')).toBe('light-blue');
    expect(resolveUiThemeId('orange-dark')).toBe('orange-dark');
  });
});
