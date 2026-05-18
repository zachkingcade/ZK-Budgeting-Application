export type AccountingPeriodFrequencyUnit = 'DAYS' | 'WEEKS' | 'MONTHS';

export const ACCOUNTING_PERIOD_SETTING_KEYS = {
  frequencyUnit: 'accounting_period.frequency_unit',
  frequencyCount: 'accounting_period.frequency_count',
  firstStartDate: 'accounting_period.first_start_date',
} as const;

export interface AccountingPeriodSettingsForm {
  frequencyUnit: AccountingPeriodFrequencyUnit | '';
  frequencyCount: number | null;
  firstStartDate: string;
}

export const EMPTY_ACCOUNTING_PERIOD_SETTINGS: AccountingPeriodSettingsForm = {
  frequencyUnit: '',
  frequencyCount: null,
  firstStartDate: '',
};

export function maxFrequencyCount(unit: AccountingPeriodFrequencyUnit): number {
  switch (unit) {
    case 'DAYS':
      return 365;
    case 'WEEKS':
      return 52;
    case 'MONTHS':
      return 12;
  }
}
