package zachkingcade.dev.ledger.adapter.in.web.dto.budgetplan.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class BudgetPlanRequests {

    private BudgetPlanRequests() {
    }

    public record CreateBudgetPlanRequest(String name) {}

    public record RenameBudgetPlanRequest(String name) {}

    public record DuplicateBudgetPlanRequest(
            String name, boolean copyIncomes, boolean copyRecurringPayables, boolean copyBudgetAmounts) {}

    public record CreateBpIncomeRequest(
            long amountMinor, String frequencyUnit, int frequencyNumber, long accountId) {}

    public record UpdateBpIncomeRequest(
            long amountMinor, String frequencyUnit, int frequencyNumber, long accountId) {}

    public record CreateBpRecurringPayableRequest(
            String description,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber,
            long budgetAccountId) {}

    public record UpdateBpRecurringPayableRequest(
            String description,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber,
            long budgetAccountId) {}

    public record CreateBpBudgetRequest(
            @JsonProperty("amountMinor") @JsonAlias("targetAmountPerDayMinor") long amountMinor,
            String frequencyUnit,
            int frequencyNumber,
            long accountId) {}

    public record UpdateBpBudgetRequest(
            @JsonProperty("amountMinor") @JsonAlias("targetAmountPerDayMinor") long amountMinor,
            String frequencyUnit,
            int frequencyNumber,
            long accountId) {}

    public record CreateBpFundingLineRequest(
            long bpIncomeId, long amountMinor, String frequencyUnit, int frequencyNumber) {}

    public record UpdateBpFundingLineRequest(
            long bpIncomeId, long amountMinor, String frequencyUnit, int frequencyNumber) {}
}
