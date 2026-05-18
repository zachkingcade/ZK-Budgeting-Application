/** Ledger API shapes for `/budget-plans` (camelCase matches Spring JSON). */

export interface IBudgetPlanListItem {
  budgetPlanId: number;
  name: string;
  createdAt: string;
  lastEditedAt: string;
}

export interface IBpIncomeResponse {
  bpIncomeId: number;
  budgetPlanId: number;
  amountMinor: number;
  frequencyUnit: string;
  frequencyNumber: number;
  accountId: number;
  accountDescription: string;
  amountPerDayMinor: number;
  amountPerWeekMinor: number;
  amountPerMonthMinor: number;
  amountPerYearMinor: number;
}

export interface IBpRecurringPayableResponse {
  bpRecurringPayableId: number;
  budgetPlanId: number;
  description: string;
  amountMinor: number;
  frequencyUnit: string;
  frequencyNumber: number;
  budgetAccountId: number;
  budgetAccountDescription: string;
  amountPerDayMinor: number;
  amountPerWeekMinor: number;
  amountPerMonthMinor: number;
  amountPerYearMinor: number;
}

export interface IBpBudgetResponse {
  bpBudgetId: number;
  budgetPlanId: number;
  targetAmountPerDayMinor: number;
  targetAmountPerWeekMinor: number;
  targetAmountPerMonthMinor: number;
  targetAmountPerYearMinor: number;
  accountId: number;
  accountDescription: string;
}

export interface IBpFundingLineResponse {
  bpFundingLineId: number;
  bpBudgetId: number;
  bpIncomeId: number;
  incomeAccountDescription: string;
  amountMinor: number;
  frequencyUnit: string;
  frequencyNumber: number;
  amountPerDayMinor: number;
  amountPerWeekMinor: number;
  amountPerMonthMinor: number;
  amountPerYearMinor: number;
}

export interface IBpBudgetWithFundingResponse {
  budget: IBpBudgetResponse;
  fundingLines: IBpFundingLineResponse[];
}

export interface IIncomeFundingSummaryView {
  bpIncomeId: number;
  incomeAccountDescription: string;
  incomeAmountPerDayMinor: number;
  incomeAmountPerWeekMinor: number;
  incomeAmountPerMonthMinor: number;
  incomeAmountPerYearMinor: number;
  allocatedPerDayMinor: number;
  allocatedPerWeekMinor: number;
  allocatedPerMonthMinor: number;
  allocatedPerYearMinor: number;
  remainingPerDayMinor: number;
  remainingPerWeekMinor: number;
  remainingPerMonthMinor: number;
  remainingPerYearMinor: number;
}

export interface IBudgetPlanEditorView {
  plan: IBudgetPlanListItem;
  incomes: IBpIncomeResponse[];
  recurringPayables: IBpRecurringPayableResponse[];
  budgets: IBpBudgetWithFundingResponse[];
  incomeFundingSummaries: IIncomeFundingSummaryView[];
}

export interface ICreateBudgetPlanRequest {
  name: string;
}

export interface IRenameBudgetPlanRequest {
  name: string;
}

export interface IDuplicateBudgetPlanRequest {
  name: string;
  copyIncomes: boolean;
  copyRecurringPayables: boolean;
  copyBudgetAmounts: boolean;
}

export interface ICreateBpIncomeRequest {
  amountMinor: number;
  frequencyUnit: string;
  frequencyNumber: number;
  accountId: number;
}

export type IUpdateBpIncomeRequest = ICreateBpIncomeRequest;

export interface ICreateBpRecurringPayableRequest {
  description: string;
  amountMinor: number;
  frequencyUnit: string;
  frequencyNumber: number;
  budgetAccountId: number;
}

export type IUpdateBpRecurringPayableRequest = ICreateBpRecurringPayableRequest;

export interface ICreateBpBudgetRequest {
  amountMinor: number;
  frequencyUnit: string;
  frequencyNumber: number;
  accountId: number;
}

export type IUpdateBpBudgetRequest = ICreateBpBudgetRequest;

export interface ICreateBpFundingLineRequest {
  bpIncomeId: number;
  amountMinor: number;
  frequencyUnit: string;
  frequencyNumber: number;
}

export type IUpdateBpFundingLineRequest = ICreateBpFundingLineRequest;
