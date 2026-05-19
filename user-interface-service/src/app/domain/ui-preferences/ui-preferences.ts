import { AccountingPeriodSettingsForm, EMPTY_ACCOUNTING_PERIOD_SETTINGS } from '../accounting-period/accounting-period-settings';
import {
  AccountTypesDisplaySettingsForm,
  EMPTY_ACCOUNT_TYPES_DISPLAY_SETTINGS,
} from './account-types-display-settings';
import { AccountsDisplaySettingsForm, EMPTY_ACCOUNTS_DISPLAY_SETTINGS } from './accounts-display-settings';
import { EMPTY_LEDGER_DISPLAY_SETTINGS, LedgerDisplaySettingsForm } from './ledger-display-settings';
import { DEFAULT_UI_THEME, UiThemeId } from './ui-theme-settings';

export interface UiPreferencesForm {
  accountingPeriod: AccountingPeriodSettingsForm;
  ledger: LedgerDisplaySettingsForm;
  accounts: AccountsDisplaySettingsForm;
  accountTypes: AccountTypesDisplaySettingsForm;
  theme: UiThemeId;
}

export const EMPTY_UI_PREFERENCES: UiPreferencesForm = {
  accountingPeriod: { ...EMPTY_ACCOUNTING_PERIOD_SETTINGS },
  ledger: { ...EMPTY_LEDGER_DISPLAY_SETTINGS },
  accounts: { ...EMPTY_ACCOUNTS_DISPLAY_SETTINGS },
  accountTypes: { ...EMPTY_ACCOUNT_TYPES_DISPLAY_SETTINGS },
  theme: DEFAULT_UI_THEME,
};
