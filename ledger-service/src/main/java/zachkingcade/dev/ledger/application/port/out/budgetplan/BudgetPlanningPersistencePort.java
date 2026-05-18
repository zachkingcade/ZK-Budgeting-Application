package zachkingcade.dev.ledger.application.port.out.budgetplan;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BudgetPlanningPersistencePort {

    record PlanRow(Long id, Long userId, String name, Instant createdAt, Instant lastEditedAt) {}

    record IncomeRow(Long id, Long budgetPlanId, long amountMinor, String frequencyUnit, int frequencyNumber, Long accountId) {}

    record PayableRow(
            Long id,
            Long budgetPlanId,
            String description,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber,
            Long budgetAccountId) {}

    record BudgetRow(Long id, Long budgetPlanId, java.math.BigDecimal targetAmountPerDayMinor, Long accountId) {}

    record FundingLineRow(
            Long id,
            Long bpBudgetId,
            Long bpIncomeId,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber) {}

    List<PlanRow> findAllPlansForUser(Long userId);

    Optional<PlanRow> findPlan(Long userId, Long planId);

    PlanRow insertPlan(Long userId, String name);

    void updatePlanName(Long userId, Long planId, String name);

    void deletePlan(Long userId, Long planId);

    void touchPlan(Long planId);

    List<IncomeRow> listIncomes(Long planId);

    Optional<IncomeRow> findIncome(Long planId, Long incomeId);

    IncomeRow insertIncome(IncomeRow rowWithoutId);

    void updateIncome(Long planId, Long incomeId, long amountMinor, String frequencyUnit, int frequencyNumber, Long accountId);

    void deleteIncome(Long planId, Long incomeId);

    List<PayableRow> listPayables(Long planId);

    Optional<PayableRow> findPayable(Long planId, Long payableId);

    PayableRow insertPayable(PayableRow rowWithoutId);

    void updatePayable(
            Long planId,
            Long payableId,
            String description,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber,
            Long budgetAccountId);

    void deletePayable(Long planId, Long payableId);

    List<BudgetRow> listBudgets(Long planId);

    Optional<BudgetRow> findBudget(Long planId, Long budgetId);

    Optional<BudgetRow> findBudgetById(Long budgetId);

    BudgetRow insertBudget(BudgetRow rowWithoutId);

    void updateBudget(Long planId, Long budgetId, java.math.BigDecimal targetAmountPerDayMinor, Long accountId);

    void deleteBudget(Long planId, Long budgetId);

    List<FundingLineRow> listFundingLines(Long bpBudgetId);

    Optional<FundingLineRow> findFundingLine(Long bpBudgetId, Long lineId);

    FundingLineRow insertFundingLine(FundingLineRow rowWithoutId);

    void updateFundingLine(
            Long bpBudgetId,
            Long lineId,
            Long bpIncomeId,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber);

    void deleteFundingLine(Long bpBudgetId, Long lineId);

    List<FundingLineRow> listFundingLinesForPlan(Long planId);
}
