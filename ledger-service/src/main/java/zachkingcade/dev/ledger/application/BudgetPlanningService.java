package zachkingcade.dev.ledger.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zachkingcade.dev.ledger.application.budgetplan.BudgetPlanningViews.*;
import zachkingcade.dev.ledger.application.commands.account.GetByIdAccountCommand;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.exception.NotFoundException;
import zachkingcade.dev.ledger.application.port.in.account.GetByIdAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.budgetplan.BudgetPlanningUseCase;
import zachkingcade.dev.ledger.application.port.out.account.AccountRepositoryPort;
import zachkingcade.dev.ledger.application.port.out.budgetplan.BudgetPlanningPersistencePort;
import zachkingcade.dev.ledger.application.port.out.budgetplan.BudgetPlanningPersistencePort.*;
import zachkingcade.dev.ledger.domain.budgetplan.BudgetPlanFrequencyMath;

import java.math.BigDecimal;
import java.util.*;

@Service
public class BudgetPlanningService implements BudgetPlanningUseCase {

    private static final String REVENUE_CLASSIFICATION = "Revenue";
    /** Asset types allowed for plan budgets and recurring-payable links (matches system types in V4__System_Account_Types). */
    private static final List<String> BUDGET_PLAN_POOL_ACCOUNT_TYPE_DESCRIPTIONS =
            List.of("Budget", "Fund", "Savings", "Investment");

    private static final Set<String> FREQUENCY_UNITS = Set.of("days", "weeks", "months", "years");

    private final BudgetPlanningPersistencePort persistence;
    private final AccountRepositoryPort accountRepositoryPort;
    private final GetByIdAccountUseCase getByIdAccountUseCase;

    public BudgetPlanningService(
            BudgetPlanningPersistencePort persistence,
            AccountRepositoryPort accountRepositoryPort,
            GetByIdAccountUseCase getByIdAccountUseCase) {
        this.persistence = persistence;
        this.accountRepositoryPort = accountRepositoryPort;
        this.getByIdAccountUseCase = getByIdAccountUseCase;
    }

    @Override
    public List<BudgetPlanListItem> listPlans(Long userId) {
        return persistence.findAllPlansForUser(userId).stream().map(this::toListItem).toList();
    }

    @Override
    public BudgetPlanListItem createPlan(Long userId, String name) {
        requireNonBlankName(name);
        PlanRow saved = persistence.insertPlan(userId, name.trim());
        return toListItem(saved);
    }

    @Override
    public BudgetPlanListItem renamePlan(Long userId, Long planId, String name) {
        requireNonBlankName(name);
        requirePlan(userId, planId);
        persistence.updatePlanName(userId, planId, name.trim());
        return toListItem(persistence.findPlan(userId, planId).orElseThrow());
    }

    @Override
    @Transactional
    public BudgetPlanListItem duplicatePlan(
            Long userId,
            Long sourcePlanId,
            String name,
            boolean copyIncomes,
            boolean copyRecurringPayables,
            boolean copyBudgetAmounts) {
        requireNonBlankName(name);
        requirePlan(userId, sourcePlanId);
        PlanRow newPlan = persistence.insertPlan(userId, name.trim());
        Long newPlanId = newPlan.id();

        Map<Long, Long> incomeIdMap = new HashMap<>();
        if (copyIncomes) {
            for (IncomeRow src : persistence.listIncomes(sourcePlanId)) {
                IncomeRow saved = persistence.insertIncome(new IncomeRow(
                        null, newPlanId, src.amountMinor(), src.frequencyUnit(), src.frequencyNumber(), src.accountId()));
                incomeIdMap.put(src.id(), saved.id());
            }
        }

        if (copyRecurringPayables) {
            for (PayableRow src : persistence.listPayables(sourcePlanId)) {
                persistence.insertPayable(new PayableRow(
                        null,
                        newPlanId,
                        src.description(),
                        src.amountMinor(),
                        src.frequencyUnit(),
                        src.frequencyNumber(),
                        src.budgetAccountId()));
            }
        }

        Map<Long, Long> budgetIdMap = new HashMap<>();
        if (copyBudgetAmounts) {
            for (BudgetRow src : persistence.listBudgets(sourcePlanId)) {
                BudgetRow saved = persistence.insertBudget(
                        new BudgetRow(null, newPlanId, src.targetAmountPerDayMinor(), src.accountId()));
                budgetIdMap.put(src.id(), saved.id());
            }
        }

        if (copyBudgetAmounts && copyIncomes) {
            for (FundingLineRow src : persistence.listFundingLinesForPlan(sourcePlanId)) {
                Long newBudgetId = budgetIdMap.get(src.bpBudgetId());
                Long newIncomeId = incomeIdMap.get(src.bpIncomeId());
                if (newBudgetId != null && newIncomeId != null) {
                    persistence.insertFundingLine(new FundingLineRow(
                            null,
                            newBudgetId,
                            newIncomeId,
                            src.amountMinor(),
                            src.frequencyUnit(),
                            src.frequencyNumber()));
                }
            }
        }

        persistence.touchPlan(newPlanId);
        return toListItem(newPlan);
    }

    @Override
    public void deletePlan(Long userId, Long planId) {
        requirePlan(userId, planId);
        persistence.deletePlan(userId, planId);
    }

    @Override
    public BudgetPlanEditorView loadEditor(Long userId, Long planId) {
        PlanRow plan = requirePlan(userId, planId);
        Map<Long, String> accountLabels = new HashMap<>();
        List<BpIncomeResponse> incomes = listIncomesInternal(userId, planId, accountLabels);
        List<BpRecurringPayableResponse> payables = listPayablesInternal(userId, planId, accountLabels);
        List<BpBudgetWithFundingResponse> budgets = listBudgetsWithFundingInternal(userId, planId, accountLabels);
        List<IncomeFundingSummaryView> summaries = buildIncomeSummaries(planId, incomes);
        return new BudgetPlanEditorView(toListItem(plan), incomes, payables, budgets, summaries);
    }

    @Override
    public List<BpIncomeResponse> listIncomes(Long userId, Long planId) {
        requirePlan(userId, planId);
        return listIncomesInternal(userId, planId, new HashMap<>());
    }

    @Override
    public BpIncomeResponse createIncome(
            Long userId, Long planId, long amountMinor, String frequencyUnit, int frequencyNumber, long accountId) {
        requirePlan(userId, planId);
        validateNonZeroAmount(amountMinor);
        validateFrequency(frequencyUnit, frequencyNumber);
        assertRevenueAccount(userId, accountId);
        IncomeRow row = new IncomeRow(null, planId, amountMinor, frequencyUnit, frequencyNumber, accountId);
        IncomeRow saved = persistence.insertIncome(row);
        persistence.touchPlan(planId);
        return toIncomeResponse(userId, saved, new HashMap<>());
    }

    @Override
    public BpIncomeResponse updateIncome(
            Long userId,
            Long planId,
            Long incomeId,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber,
            long accountId) {
        requirePlan(userId, planId);
        persistence.findIncome(planId, incomeId).orElseThrow(() -> new NotFoundException("Income not found"));
        validateNonZeroAmount(amountMinor);
        validateFrequency(frequencyUnit, frequencyNumber);
        assertRevenueAccount(userId, accountId);
        persistence.updateIncome(planId, incomeId, amountMinor, frequencyUnit, frequencyNumber, accountId);
        persistence.touchPlan(planId);
        IncomeRow updated =
                persistence.findIncome(planId, incomeId).orElseThrow(() -> new NotFoundException("Income not found"));
        return toIncomeResponse(userId, updated, new HashMap<>());
    }

    @Override
    public void deleteIncome(Long userId, Long planId, Long incomeId) {
        requirePlan(userId, planId);
        persistence.deleteIncome(planId, incomeId);
        persistence.touchPlan(planId);
    }

    @Override
    public List<BpRecurringPayableResponse> listRecurringPayables(Long userId, Long planId) {
        requirePlan(userId, planId);
        return listPayablesInternal(userId, planId, new HashMap<>());
    }

    @Override
    public BpRecurringPayableResponse createRecurringPayable(
            Long userId,
            Long planId,
            String description,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber,
            long budgetAccountId) {
        requirePlan(userId, planId);
        requireNonBlankDescription(description);
        validateNonZeroAmount(amountMinor);
        validateFrequency(frequencyUnit, frequencyNumber);
        assertBudgetAccount(userId, budgetAccountId);
        PayableRow saved = persistence.insertPayable(
                new PayableRow(null, planId, description.trim(), amountMinor, frequencyUnit, frequencyNumber, budgetAccountId));
        persistence.touchPlan(planId);
        return toPayableResponse(userId, saved, new HashMap<>());
    }

    @Override
    public BpRecurringPayableResponse updateRecurringPayable(
            Long userId,
            Long planId,
            Long payableId,
            String description,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber,
            long budgetAccountId) {
        requirePlan(userId, planId);
        persistence.findPayable(planId, payableId).orElseThrow(() -> new NotFoundException("Recurring payable not found"));
        requireNonBlankDescription(description);
        validateNonZeroAmount(amountMinor);
        validateFrequency(frequencyUnit, frequencyNumber);
        assertBudgetAccount(userId, budgetAccountId);
        persistence.updatePayable(planId, payableId, description.trim(), amountMinor, frequencyUnit, frequencyNumber, budgetAccountId);
        persistence.touchPlan(planId);
        PayableRow updated = persistence
                .findPayable(planId, payableId)
                .orElseThrow(() -> new NotFoundException("Recurring payable not found"));
        return toPayableResponse(userId, updated, new HashMap<>());
    }

    @Override
    public void deleteRecurringPayable(Long userId, Long planId, Long payableId) {
        requirePlan(userId, planId);
        persistence.deletePayable(planId, payableId);
        persistence.touchPlan(planId);
    }

    @Override
    public List<BpBudgetResponse> listBudgets(Long userId, Long planId) {
        requirePlan(userId, planId);
        return persistence.listBudgets(planId).stream().map(b -> toBudgetResponse(userId, b, new HashMap<>())).toList();
    }

    @Override
    public BpBudgetResponse createBudget(
            Long userId, Long planId, long amountMinor, String frequencyUnit, int frequencyNumber, long accountId) {
        requirePlan(userId, planId);
        validateNonZeroAmount(amountMinor);
        validateFrequency(frequencyUnit, frequencyNumber);
        assertBudgetAccount(userId, accountId);
        BigDecimal targetPerDayMinor =
                BudgetPlanFrequencyMath.amountPerDayMinorBd(amountMinor, frequencyUnit, frequencyNumber);
        validateNonZeroBigDecimal(targetPerDayMinor);
        BudgetRow saved = persistence.insertBudget(new BudgetRow(null, planId, targetPerDayMinor, accountId));
        persistence.touchPlan(planId);
        return toBudgetResponse(userId, saved, new HashMap<>());
    }

    @Override
    public BpBudgetResponse updateBudget(
            Long userId,
            Long planId,
            Long budgetId,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber,
            long accountId) {
        requirePlan(userId, planId);
        persistence.findBudget(planId, budgetId).orElseThrow(() -> new NotFoundException("Budget not found"));
        validateNonZeroAmount(amountMinor);
        validateFrequency(frequencyUnit, frequencyNumber);
        assertBudgetAccount(userId, accountId);
        BigDecimal targetPerDayMinor =
                BudgetPlanFrequencyMath.amountPerDayMinorBd(amountMinor, frequencyUnit, frequencyNumber);
        validateNonZeroBigDecimal(targetPerDayMinor);
        persistence.updateBudget(planId, budgetId, targetPerDayMinor, accountId);
        persistence.touchPlan(planId);
        BudgetRow updated =
                persistence.findBudget(planId, budgetId).orElseThrow(() -> new NotFoundException("Budget not found"));
        return toBudgetResponse(userId, updated, new HashMap<>());
    }

    @Override
    public void deleteBudget(Long userId, Long planId, Long budgetId) {
        requirePlan(userId, planId);
        persistence.deleteBudget(planId, budgetId);
        persistence.touchPlan(planId);
    }

    @Override
    public List<BpFundingLineResponse> listFundingLines(Long userId, Long planId, Long budgetId) {
        requirePlan(userId, planId);
        requireBudget(planId, budgetId);
        return persistence.listFundingLines(budgetId).stream()
                .map(l -> toFundingLineResponse(userId, planId, l, new HashMap<>()))
                .toList();
    }

    @Override
    public BpFundingLineResponse createFundingLine(
            Long userId,
            Long planId,
            Long budgetId,
            long bpIncomeId,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber) {
        requirePlan(userId, planId);
        requireBudget(planId, budgetId);
        persistence.findIncome(planId, bpIncomeId).orElseThrow(() -> new NotFoundException("Income not found for plan"));
        validateNonZeroAmount(amountMinor);
        validateFrequency(frequencyUnit, frequencyNumber);
        FundingLineRow saved = persistence.insertFundingLine(
                new FundingLineRow(null, budgetId, bpIncomeId, amountMinor, frequencyUnit, frequencyNumber));
        persistence.touchPlan(planId);
        return toFundingLineResponse(userId, planId, saved, new HashMap<>());
    }

    @Override
    public BpFundingLineResponse updateFundingLine(
            Long userId,
            Long planId,
            Long budgetId,
            Long lineId,
            long bpIncomeId,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber) {
        requirePlan(userId, planId);
        requireBudget(planId, budgetId);
        persistence.findFundingLine(budgetId, lineId).orElseThrow(() -> new NotFoundException("Funding line not found"));
        persistence.findIncome(planId, bpIncomeId).orElseThrow(() -> new NotFoundException("Income not found for plan"));
        validateNonZeroAmount(amountMinor);
        validateFrequency(frequencyUnit, frequencyNumber);
        persistence.updateFundingLine(budgetId, lineId, bpIncomeId, amountMinor, frequencyUnit, frequencyNumber);
        persistence.touchPlan(planId);
        FundingLineRow updated = persistence
                .findFundingLine(budgetId, lineId)
                .orElseThrow(() -> new NotFoundException("Funding line not found"));
        return toFundingLineResponse(userId, planId, updated, new HashMap<>());
    }

    @Override
    public void deleteFundingLine(Long userId, Long planId, Long budgetId, Long lineId) {
        requirePlan(userId, planId);
        requireBudget(planId, budgetId);
        persistence.deleteFundingLine(budgetId, lineId);
        persistence.touchPlan(planId);
    }

    private PlanRow requirePlan(Long userId, Long planId) {
        return persistence.findPlan(userId, planId).orElseThrow(() -> new NotFoundException("Budget plan not found"));
    }

    private void requireBudget(Long planId, Long budgetId) {
        persistence.findBudget(planId, budgetId).orElseThrow(() -> new NotFoundException("Budget not found"));
    }

    private void requireNonBlankName(String name) {
        if (name == null || name.isBlank()) {
            throw new ApplicationException("Plan name is required");
        }
    }

    private void requireNonBlankDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new ApplicationException("Description is required");
        }
    }

    private void validateNonZeroAmount(long amountMinor) {
        if (amountMinor == 0) {
            throw new ApplicationException("Amount must be non-zero");
        }
    }

    private void validateNonZeroBigDecimal(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new ApplicationException("Amount must be non-zero");
        }
    }

    private void validateFrequency(String frequencyUnit, int frequencyNumber) {
        if (frequencyUnit == null || !FREQUENCY_UNITS.contains(frequencyUnit)) {
            throw new ApplicationException("frequencyUnit must be one of: days, weeks, months, years");
        }
        if (frequencyNumber < 1) {
            throw new ApplicationException("frequencyNumber must be >= 1");
        }
        try {
            BudgetPlanFrequencyMath.periodDays(frequencyUnit, frequencyNumber);
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(e.getMessage());
        }
    }

    private void assertRevenueAccount(Long userId, long accountId) {
        if (!accountRepositoryPort.accountHasClassification(userId, accountId, REVENUE_CLASSIFICATION)) {
            throw new ApplicationException("Income lines must use a Revenue-classification account");
        }
    }

    private void assertBudgetAccount(Long userId, long accountId) {
        if (!accountRepositoryPort.accountHasAnyOfTypeDescriptions(
                userId, accountId, BUDGET_PLAN_POOL_ACCOUNT_TYPE_DESCRIPTIONS)) {
            throw new ApplicationException(
                    "Account must be one of: Budget, Fund, Savings, Investment (asset pool types)");
        }
    }

    private BudgetPlanListItem toListItem(PlanRow p) {
        return new BudgetPlanListItem(p.id(), p.name(), p.createdAt(), p.lastEditedAt());
    }

    private List<BpIncomeResponse> listIncomesInternal(Long userId, Long planId, Map<Long, String> accountLabels) {
        return persistence.listIncomes(planId).stream()
                .map(r -> toIncomeResponse(userId, r, accountLabels))
                .toList();
    }

    private List<BpRecurringPayableResponse> listPayablesInternal(Long userId, Long planId, Map<Long, String> accountLabels) {
        return persistence.listPayables(planId).stream()
                .map(r -> toPayableResponse(userId, r, accountLabels))
                .toList();
    }

    private List<BpBudgetWithFundingResponse> listBudgetsWithFundingInternal(
            Long userId, Long planId, Map<Long, String> accountLabels) {
        return persistence.listBudgets(planId).stream()
                .map(b -> {
                    BpBudgetResponse budget = toBudgetResponse(userId, b, accountLabels);
                    List<BpFundingLineResponse> lines = persistence.listFundingLines(b.id()).stream()
                            .map(l -> toFundingLineResponse(userId, planId, l, accountLabels))
                            .toList();
                    return new BpBudgetWithFundingResponse(budget, lines);
                })
                .toList();
    }

    private List<IncomeFundingSummaryView> buildIncomeSummaries(Long planId, List<BpIncomeResponse> incomes) {
        Map<Long, BigDecimal> allocatedPerDayByIncome = new HashMap<>();
        for (FundingLineRow line : persistence.listFundingLinesForPlan(planId)) {
            BigDecimal perDay = BudgetPlanFrequencyMath.amountPerDayMinorBd(
                    line.amountMinor(), line.frequencyUnit(), line.frequencyNumber());
            allocatedPerDayByIncome.merge(line.bpIncomeId(), perDay, BigDecimal::add);
        }
        List<IncomeFundingSummaryView> out = new ArrayList<>();
        for (BpIncomeResponse inc : incomes) {
            BigDecimal allocated = allocatedPerDayByIncome.getOrDefault(inc.bpIncomeId(), BigDecimal.ZERO);
            BigDecimal remainingDay = inc.amountPerDayMinor().subtract(allocated);
            FrequencyAmountsMinor incFull = new FrequencyAmountsMinor(
                    inc.amountPerDayMinor(),
                    inc.amountPerWeekMinor(),
                    inc.amountPerMonthMinor(),
                    inc.amountPerYearMinor());
            FrequencyAmountsMinor alloc = enrichFromPerDay(allocated);
            FrequencyAmountsMinor rem = enrichFromPerDay(remainingDay);
            out.add(new IncomeFundingSummaryView(
                    inc.bpIncomeId(),
                    inc.accountDescription(),
                    incFull.amountPerDayMinor(),
                    incFull.amountPerWeekMinor(),
                    incFull.amountPerMonthMinor(),
                    incFull.amountPerYearMinor(),
                    alloc.amountPerDayMinor(),
                    alloc.amountPerWeekMinor(),
                    alloc.amountPerMonthMinor(),
                    alloc.amountPerYearMinor(),
                    rem.amountPerDayMinor(),
                    rem.amountPerWeekMinor(),
                    rem.amountPerMonthMinor(),
                    rem.amountPerYearMinor()));
        }
        return out;
    }

    private BpIncomeResponse toIncomeResponse(Long userId, IncomeRow r, Map<Long, String> accountLabels) {
        FrequencyAmountsMinor a = enrichRecurringAmount(r.amountMinor(), r.frequencyUnit(), r.frequencyNumber());
        String desc = accountDescription(userId, r.accountId(), accountLabels);
        return new BpIncomeResponse(
                r.id(),
                r.budgetPlanId(),
                r.amountMinor(),
                r.frequencyUnit(),
                r.frequencyNumber(),
                r.accountId(),
                desc,
                a.amountPerDayMinor(),
                a.amountPerWeekMinor(),
                a.amountPerMonthMinor(),
                a.amountPerYearMinor());
    }

    private BpRecurringPayableResponse toPayableResponse(Long userId, PayableRow r, Map<Long, String> accountLabels) {
        FrequencyAmountsMinor a = enrichRecurringAmount(r.amountMinor(), r.frequencyUnit(), r.frequencyNumber());
        String budgetDesc = accountDescription(userId, r.budgetAccountId(), accountLabels);
        return new BpRecurringPayableResponse(
                r.id(),
                r.budgetPlanId(),
                r.description(),
                r.amountMinor(),
                r.frequencyUnit(),
                r.frequencyNumber(),
                r.budgetAccountId(),
                budgetDesc,
                a.amountPerDayMinor(),
                a.amountPerWeekMinor(),
                a.amountPerMonthMinor(),
                a.amountPerYearMinor());
    }

    private BpBudgetResponse toBudgetResponse(Long userId, BudgetRow r, Map<Long, String> accountLabels) {
        BigDecimal perDay = r.targetAmountPerDayMinor();
        FrequencyAmountsMinor a = enrichFromPerDay(perDay);
        String desc = accountDescription(userId, r.accountId(), accountLabels);
        return new BpBudgetResponse(
                r.id(),
                r.budgetPlanId(),
                perDay,
                a.amountPerWeekMinor(),
                a.amountPerMonthMinor(),
                a.amountPerYearMinor(),
                r.accountId(),
                desc);
    }

    private BpFundingLineResponse toFundingLineResponse(
            Long userId, Long planId, FundingLineRow r, Map<Long, String> accountLabels) {
        IncomeRow income = persistence
                .findIncome(planId, r.bpIncomeId())
                .orElseThrow(() -> new NotFoundException("Income not found for funding line"));
        FrequencyAmountsMinor a = enrichRecurringAmount(r.amountMinor(), r.frequencyUnit(), r.frequencyNumber());
        String incomeAccountDesc = accountDescription(userId, income.accountId(), accountLabels);
        return new BpFundingLineResponse(
                r.id(),
                r.bpBudgetId(),
                r.bpIncomeId(),
                incomeAccountDesc,
                r.amountMinor(),
                r.frequencyUnit(),
                r.frequencyNumber(),
                a.amountPerDayMinor(),
                a.amountPerWeekMinor(),
                a.amountPerMonthMinor(),
                a.amountPerYearMinor());
    }

    private FrequencyAmountsMinor enrichRecurringAmount(long amountMinor, String frequencyUnit, int frequencyNumber) {
        BigDecimal perDay = BudgetPlanFrequencyMath.amountPerDayMinorBd(amountMinor, frequencyUnit, frequencyNumber);
        return enrichFromPerDay(perDay);
    }

    private FrequencyAmountsMinor enrichFromPerDay(BigDecimal amountPerDayMinor) {
        return new FrequencyAmountsMinor(
                amountPerDayMinor,
                BudgetPlanFrequencyMath.perWeekFromPerDay(amountPerDayMinor),
                BudgetPlanFrequencyMath.perMonthFromPerDay(amountPerDayMinor),
                BudgetPlanFrequencyMath.perYearFromPerDay(amountPerDayMinor));
    }

    private String accountDescription(Long userId, Long accountId, Map<Long, String> cache) {
        return cache.computeIfAbsent(
                accountId,
                id -> getByIdAccountUseCase
                        .getAccountById(new GetByIdAccountCommand(userId, id))
                        .description());
    }
}
