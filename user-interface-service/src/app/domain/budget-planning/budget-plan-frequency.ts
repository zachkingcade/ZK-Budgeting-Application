/** Matches ledger `BudgetPlanFrequencyMath` (30-day month, 365-day year). */

export function budgetPlanPeriodDays(frequencyUnit: string, frequencyNumber: number): number {
  if (frequencyNumber < 1) {
    return 0;
  }
  switch (frequencyUnit) {
    case 'days':
      return frequencyNumber;
    case 'weeks':
      return frequencyNumber * 7;
    case 'months':
      return frequencyNumber * 30;
    case 'years':
      return frequencyNumber * 365;
    default:
      return 0;
  }
}

/** Funding per day × length of one income pay period → minor units per income payment. */
export function fundingPerIncomePayPeriodMinor(
  fundingAmountPerDayMinor: number,
  incomeFrequencyUnit: string,
  incomeFrequencyNumber: number,
): number {
  const days = budgetPlanPeriodDays(incomeFrequencyUnit, incomeFrequencyNumber);
  if (days <= 0) {
    return 0;
  }
  return fundingAmountPerDayMinor * days;
}

export function formatIncomePayPeriodLabel(frequencyNumber: number, frequencyUnit: string): string {
  return `every ${frequencyNumber} ${frequencyUnit}`;
}
