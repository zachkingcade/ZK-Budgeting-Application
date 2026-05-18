package zachkingcade.dev.ledger.application.port.in.budgetplan;

import zachkingcade.dev.ledger.application.budgetplan.BudgetPlanningViews.*;

public interface BudgetPlanningUseCase {

    java.util.List<BudgetPlanListItem> listPlans(Long userId);

    BudgetPlanListItem createPlan(Long userId, String name);

    BudgetPlanListItem renamePlan(Long userId, Long planId, String name);

    BudgetPlanListItem duplicatePlan(
            Long userId,
            Long sourcePlanId,
            String name,
            boolean copyIncomes,
            boolean copyRecurringPayables,
            boolean copyBudgetAmounts);

    void deletePlan(Long userId, Long planId);

    BudgetPlanEditorView loadEditor(Long userId, Long planId);

    java.util.List<BpIncomeResponse> listIncomes(Long userId, Long planId);

    BpIncomeResponse createIncome(
            Long userId, Long planId, long amountMinor, String frequencyUnit, int frequencyNumber, long accountId);

    BpIncomeResponse updateIncome(
            Long userId,
            Long planId,
            Long incomeId,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber,
            long accountId);

    void deleteIncome(Long userId, Long planId, Long incomeId);

    java.util.List<BpRecurringPayableResponse> listRecurringPayables(Long userId, Long planId);

    BpRecurringPayableResponse createRecurringPayable(
            Long userId,
            Long planId,
            String description,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber,
            long budgetAccountId);

    BpRecurringPayableResponse updateRecurringPayable(
            Long userId,
            Long planId,
            Long payableId,
            String description,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber,
            long budgetAccountId);

    void deleteRecurringPayable(Long userId, Long planId, Long payableId);

    java.util.List<BpBudgetResponse> listBudgets(Long userId, Long planId);

    BpBudgetResponse createBudget(
            Long userId, Long planId, long amountMinor, String frequencyUnit, int frequencyNumber, long accountId);

    BpBudgetResponse updateBudget(
            Long userId,
            Long planId,
            Long budgetId,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber,
            long accountId);

    void deleteBudget(Long userId, Long planId, Long budgetId);

    java.util.List<BpFundingLineResponse> listFundingLines(Long userId, Long planId, Long budgetId);

    BpFundingLineResponse createFundingLine(
            Long userId,
            Long planId,
            Long budgetId,
            long bpIncomeId,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber);

    BpFundingLineResponse updateFundingLine(
            Long userId,
            Long planId,
            Long budgetId,
            Long lineId,
            long bpIncomeId,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber);

    void deleteFundingLine(Long userId, Long planId, Long budgetId, Long lineId);
}
