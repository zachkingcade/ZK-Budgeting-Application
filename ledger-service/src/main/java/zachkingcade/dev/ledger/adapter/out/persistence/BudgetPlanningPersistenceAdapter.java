package zachkingcade.dev.ledger.adapter.out.persistence;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.*;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.*;
import zachkingcade.dev.ledger.application.exception.NotFoundException;
import zachkingcade.dev.ledger.application.port.out.budgetplan.BudgetPlanningPersistencePort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetPlanningPersistenceAdapter implements BudgetPlanningPersistencePort {

    private final BudgetPlanJpaRepository budgetPlanJpaRepository;
    private final BpIncomeJpaRepository bpIncomeJpaRepository;
    private final BpRecurringPayableJpaRepository bpRecurringPayableJpaRepository;
    private final BpBudgetJpaRepository bpBudgetJpaRepository;
    private final BpFundingLineJpaRepository bpFundingLineJpaRepository;

    public BudgetPlanningPersistenceAdapter(
            BudgetPlanJpaRepository budgetPlanJpaRepository,
            BpIncomeJpaRepository bpIncomeJpaRepository,
            BpRecurringPayableJpaRepository bpRecurringPayableJpaRepository,
            BpBudgetJpaRepository bpBudgetJpaRepository,
            BpFundingLineJpaRepository bpFundingLineJpaRepository) {
        this.budgetPlanJpaRepository = budgetPlanJpaRepository;
        this.bpIncomeJpaRepository = bpIncomeJpaRepository;
        this.bpRecurringPayableJpaRepository = bpRecurringPayableJpaRepository;
        this.bpBudgetJpaRepository = bpBudgetJpaRepository;
        this.bpFundingLineJpaRepository = bpFundingLineJpaRepository;
    }

    @Override
    public List<PlanRow> findAllPlansForUser(Long userId) {
        return budgetPlanJpaRepository.findAllByUserIdOrderByLastEditedAtDesc(userId).stream().map(this::toPlanRow).toList();
    }

    @Override
    public Optional<PlanRow> findPlan(Long userId, Long planId) {
        return budgetPlanJpaRepository.findByIdAndUserId(planId, userId).map(this::toPlanRow);
    }

    @Override
    public PlanRow insertPlan(Long userId, String name) {
        Instant now = Instant.now();
        BudgetPlanEntity e = new BudgetPlanEntity();
        e.setUserId(userId);
        e.setName(name);
        e.setCreatedAt(now);
        e.setLastEditedAt(now);
        return toPlanRow(budgetPlanJpaRepository.save(e));
    }

    @Override
    public void updatePlanName(Long userId, Long planId, String name) {
        BudgetPlanEntity e = budgetPlanJpaRepository
                .findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new NotFoundException("Budget plan not found"));
        e.setName(name);
        e.setLastEditedAt(Instant.now());
        budgetPlanJpaRepository.save(e);
    }

    @Override
    public void deletePlan(Long userId, Long planId) {
        BudgetPlanEntity e = budgetPlanJpaRepository
                .findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new NotFoundException("Budget plan not found"));
        budgetPlanJpaRepository.delete(e);
    }

    @Override
    public void touchPlan(Long planId) {
        budgetPlanJpaRepository.findById(planId).ifPresent(p -> {
            p.setLastEditedAt(Instant.now());
            budgetPlanJpaRepository.save(p);
        });
    }

    @Override
    public List<IncomeRow> listIncomes(Long planId) {
        return bpIncomeJpaRepository.findAllByBudgetPlanIdOrderByIdAsc(planId).stream().map(this::toIncomeRow).toList();
    }

    @Override
    public Optional<IncomeRow> findIncome(Long planId, Long incomeId) {
        return bpIncomeJpaRepository.findByIdAndBudgetPlanId(incomeId, planId).map(this::toIncomeRow);
    }

    @Override
    public IncomeRow insertIncome(IncomeRow rowWithoutId) {
        BpIncomeEntity e = new BpIncomeEntity();
        e.setBudgetPlanId(rowWithoutId.budgetPlanId());
        e.setAmountMinor(rowWithoutId.amountMinor());
        e.setFrequencyUnit(rowWithoutId.frequencyUnit());
        e.setFrequencyNumber(rowWithoutId.frequencyNumber());
        e.setAccountId(rowWithoutId.accountId());
        return toIncomeRow(bpIncomeJpaRepository.save(e));
    }

    @Override
    public void updateIncome(Long planId, Long incomeId, long amountMinor, String frequencyUnit, int frequencyNumber, Long accountId) {
        BpIncomeEntity e = bpIncomeJpaRepository
                .findByIdAndBudgetPlanId(incomeId, planId)
                .orElseThrow(() -> new NotFoundException("Income not found"));
        e.setAmountMinor(amountMinor);
        e.setFrequencyUnit(frequencyUnit);
        e.setFrequencyNumber(frequencyNumber);
        e.setAccountId(accountId);
        bpIncomeJpaRepository.save(e);
    }

    @Override
    public void deleteIncome(Long planId, Long incomeId) {
        BpIncomeEntity e = bpIncomeJpaRepository
                .findByIdAndBudgetPlanId(incomeId, planId)
                .orElseThrow(() -> new NotFoundException("Income not found"));
        bpIncomeJpaRepository.delete(e);
    }

    @Override
    public List<PayableRow> listPayables(Long planId) {
        return bpRecurringPayableJpaRepository.findAllByBudgetPlanIdOrderByIdAsc(planId).stream()
                .map(this::toPayableRow)
                .toList();
    }

    @Override
    public Optional<PayableRow> findPayable(Long planId, Long payableId) {
        return bpRecurringPayableJpaRepository.findByIdAndBudgetPlanId(payableId, planId).map(this::toPayableRow);
    }

    @Override
    public PayableRow insertPayable(PayableRow rowWithoutId) {
        BpRecurringPayableEntity e = new BpRecurringPayableEntity();
        e.setBudgetPlanId(rowWithoutId.budgetPlanId());
        e.setDescription(rowWithoutId.description());
        e.setAmountMinor(rowWithoutId.amountMinor());
        e.setFrequencyUnit(rowWithoutId.frequencyUnit());
        e.setFrequencyNumber(rowWithoutId.frequencyNumber());
        e.setBudgetAccountId(rowWithoutId.budgetAccountId());
        return toPayableRow(bpRecurringPayableJpaRepository.save(e));
    }

    @Override
    public void updatePayable(
            Long planId,
            Long payableId,
            String description,
            long amountMinor,
            String frequencyUnit,
            int frequencyNumber,
            Long budgetAccountId) {
        BpRecurringPayableEntity e = bpRecurringPayableJpaRepository
                .findByIdAndBudgetPlanId(payableId, planId)
                .orElseThrow(() -> new NotFoundException("Recurring payable not found"));
        e.setDescription(description);
        e.setAmountMinor(amountMinor);
        e.setFrequencyUnit(frequencyUnit);
        e.setFrequencyNumber(frequencyNumber);
        e.setBudgetAccountId(budgetAccountId);
        bpRecurringPayableJpaRepository.save(e);
    }

    @Override
    public void deletePayable(Long planId, Long payableId) {
        BpRecurringPayableEntity e = bpRecurringPayableJpaRepository
                .findByIdAndBudgetPlanId(payableId, planId)
                .orElseThrow(() -> new NotFoundException("Recurring payable not found"));
        bpRecurringPayableJpaRepository.delete(e);
    }

    @Override
    public List<BudgetRow> listBudgets(Long planId) {
        return bpBudgetJpaRepository.findAllByBudgetPlanIdOrderByIdAsc(planId).stream().map(this::toBudgetRow).toList();
    }

    @Override
    public Optional<BudgetRow> findBudget(Long planId, Long budgetId) {
        return bpBudgetJpaRepository.findByIdAndBudgetPlanId(budgetId, planId).map(this::toBudgetRow);
    }

    @Override
    public Optional<BudgetRow> findBudgetById(Long budgetId) {
        return bpBudgetJpaRepository.findById(budgetId).map(this::toBudgetRow);
    }

    @Override
    public BudgetRow insertBudget(BudgetRow rowWithoutId) {
        BpBudgetEntity e = new BpBudgetEntity();
        e.setBudgetPlanId(rowWithoutId.budgetPlanId());
        e.setTargetAmountPerDayMinor(rowWithoutId.targetAmountPerDayMinor());
        e.setAccountId(rowWithoutId.accountId());
        return toBudgetRow(bpBudgetJpaRepository.save(e));
    }

    @Override
    public void updateBudget(Long planId, Long budgetId, BigDecimal targetAmountPerDayMinor, Long accountId) {
        BpBudgetEntity e = bpBudgetJpaRepository
                .findByIdAndBudgetPlanId(budgetId, planId)
                .orElseThrow(() -> new NotFoundException("Budget not found"));
        e.setTargetAmountPerDayMinor(targetAmountPerDayMinor);
        e.setAccountId(accountId);
        bpBudgetJpaRepository.save(e);
    }

    @Override
    public void deleteBudget(Long planId, Long budgetId) {
        BpBudgetEntity e = bpBudgetJpaRepository
                .findByIdAndBudgetPlanId(budgetId, planId)
                .orElseThrow(() -> new NotFoundException("Budget not found"));
        bpBudgetJpaRepository.delete(e);
    }

    @Override
    public List<FundingLineRow> listFundingLines(Long bpBudgetId) {
        return bpFundingLineJpaRepository.findAllByBpBudgetIdOrderByIdAsc(bpBudgetId).stream()
                .map(this::toFundingRow)
                .toList();
    }

    @Override
    public Optional<FundingLineRow> findFundingLine(Long bpBudgetId, Long lineId) {
        return bpFundingLineJpaRepository.findByIdAndBpBudgetId(lineId, bpBudgetId).map(this::toFundingRow);
    }

    @Override
    public FundingLineRow insertFundingLine(FundingLineRow rowWithoutId) {
        BpFundingLineEntity e = new BpFundingLineEntity();
        e.setBpBudgetId(rowWithoutId.bpBudgetId());
        e.setBpIncomeId(rowWithoutId.bpIncomeId());
        e.setAmountMinor(rowWithoutId.amountMinor());
        e.setFrequencyUnit(rowWithoutId.frequencyUnit());
        e.setFrequencyNumber(rowWithoutId.frequencyNumber());
        return toFundingRow(bpFundingLineJpaRepository.save(e));
    }

    @Override
    public void updateFundingLine(
            Long bpBudgetId, Long lineId, Long bpIncomeId, long amountMinor, String frequencyUnit, int frequencyNumber) {
        BpFundingLineEntity e = bpFundingLineJpaRepository
                .findByIdAndBpBudgetId(lineId, bpBudgetId)
                .orElseThrow(() -> new NotFoundException("Funding line not found"));
        e.setBpIncomeId(bpIncomeId);
        e.setAmountMinor(amountMinor);
        e.setFrequencyUnit(frequencyUnit);
        e.setFrequencyNumber(frequencyNumber);
        bpFundingLineJpaRepository.save(e);
    }

    @Override
    public void deleteFundingLine(Long bpBudgetId, Long lineId) {
        BpFundingLineEntity e = bpFundingLineJpaRepository
                .findByIdAndBpBudgetId(lineId, bpBudgetId)
                .orElseThrow(() -> new NotFoundException("Funding line not found"));
        bpFundingLineJpaRepository.delete(e);
    }

    @Override
    public List<FundingLineRow> listFundingLinesForPlan(Long planId) {
        return bpFundingLineJpaRepository.findAllByBudgetPlanId(planId).stream().map(this::toFundingRow).toList();
    }

    private PlanRow toPlanRow(BudgetPlanEntity e) {
        return new PlanRow(e.getId(), e.getUserId(), e.getName(), e.getCreatedAt(), e.getLastEditedAt());
    }

    private IncomeRow toIncomeRow(BpIncomeEntity e) {
        return new IncomeRow(
                e.getId(), e.getBudgetPlanId(), e.getAmountMinor(), e.getFrequencyUnit(), e.getFrequencyNumber(), e.getAccountId());
    }

    private PayableRow toPayableRow(BpRecurringPayableEntity e) {
        return new PayableRow(
                e.getId(),
                e.getBudgetPlanId(),
                e.getDescription(),
                e.getAmountMinor(),
                e.getFrequencyUnit(),
                e.getFrequencyNumber(),
                e.getBudgetAccountId());
    }

    private BudgetRow toBudgetRow(BpBudgetEntity e) {
        return new BudgetRow(e.getId(), e.getBudgetPlanId(), e.getTargetAmountPerDayMinor(), e.getAccountId());
    }

    private FundingLineRow toFundingRow(BpFundingLineEntity e) {
        return new FundingLineRow(
                e.getId(), e.getBpBudgetId(), e.getBpIncomeId(), e.getAmountMinor(), e.getFrequencyUnit(), e.getFrequencyNumber());
    }
}
