package zachkingcade.dev.ledger.application.budgetplan;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class BudgetPlanningViews {

    private BudgetPlanningViews() {
    }

    public record BudgetPlanListItem(long budgetPlanId, String name, Instant createdAt, Instant lastEditedAt) {}

    public record FrequencyAmountsMinor(
            BigDecimal amountPerDayMinor,
            BigDecimal amountPerWeekMinor,
            BigDecimal amountPerMonthMinor,
            BigDecimal amountPerYearMinor) {}

    public record BpIncomeResponse(
            long bpIncomeId,
            long budgetPlanId,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber,
            long accountId,
            String accountDescription,
            BigDecimal amountPerDayMinor,
            BigDecimal amountPerWeekMinor,
            BigDecimal amountPerMonthMinor,
            BigDecimal amountPerYearMinor) {}

    public record BpRecurringPayableResponse(
            long bpRecurringPayableId,
            long budgetPlanId,
            String description,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber,
            long budgetAccountId,
            String budgetAccountDescription,
            BigDecimal amountPerDayMinor,
            BigDecimal amountPerWeekMinor,
            BigDecimal amountPerMonthMinor,
            BigDecimal amountPerYearMinor) {}

    public record BpBudgetResponse(
            long bpBudgetId,
            long budgetPlanId,
            BigDecimal targetAmountPerDayMinor,
            BigDecimal targetAmountPerWeekMinor,
            BigDecimal targetAmountPerMonthMinor,
            BigDecimal targetAmountPerYearMinor,
            long accountId,
            String accountDescription) {}

    public record BpFundingLineResponse(
            long bpFundingLineId,
            long bpBudgetId,
            long bpIncomeId,
            String incomeAccountDescription,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber,
            BigDecimal amountPerDayMinor,
            BigDecimal amountPerWeekMinor,
            BigDecimal amountPerMonthMinor,
            BigDecimal amountPerYearMinor) {}

    public record BpBudgetWithFundingResponse(BpBudgetResponse budget, List<BpFundingLineResponse> fundingLines) {}

    public record IncomeFundingSummaryView(
            long bpIncomeId,
            String incomeAccountDescription,
            BigDecimal incomeAmountPerDayMinor,
            BigDecimal incomeAmountPerWeekMinor,
            BigDecimal incomeAmountPerMonthMinor,
            BigDecimal incomeAmountPerYearMinor,
            BigDecimal allocatedPerDayMinor,
            BigDecimal allocatedPerWeekMinor,
            BigDecimal allocatedPerMonthMinor,
            BigDecimal allocatedPerYearMinor,
            BigDecimal remainingPerDayMinor,
            BigDecimal remainingPerWeekMinor,
            BigDecimal remainingPerMonthMinor,
            BigDecimal remainingPerYearMinor) {}

    public record BudgetPlanEditorView(
            BudgetPlanListItem plan,
            List<BpIncomeResponse> incomes,
            List<BpRecurringPayableResponse> recurringPayables,
            List<BpBudgetWithFundingResponse> budgets,
            List<IncomeFundingSummaryView> incomeFundingSummaries) {}
}
