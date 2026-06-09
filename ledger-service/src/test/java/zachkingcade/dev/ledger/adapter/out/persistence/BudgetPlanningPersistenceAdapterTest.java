package zachkingcade.dev.ledger.adapter.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.BpFundingLineEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.BudgetPlanEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.BpBudgetJpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.BpFundingLineJpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.BpIncomeJpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.BpRecurringPayableJpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.BudgetPlanJpaRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetPlanningPersistenceAdapterTest {

    @Mock
    private BudgetPlanJpaRepository budgetPlanJpaRepository;

    @Mock
    private BpIncomeJpaRepository bpIncomeJpaRepository;

    @Mock
    private BpRecurringPayableJpaRepository bpRecurringPayableJpaRepository;

    @Mock
    private BpBudgetJpaRepository bpBudgetJpaRepository;

    @Mock
    private BpFundingLineJpaRepository bpFundingLineJpaRepository;

    private BudgetPlanningPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BudgetPlanningPersistenceAdapter(
                budgetPlanJpaRepository,
                bpIncomeJpaRepository,
                bpRecurringPayableJpaRepository,
                bpBudgetJpaRepository,
                bpFundingLineJpaRepository);
    }

    @Test
    void deletePlan_removesFundingLinesBeforeDeletingPlan() {
        BudgetPlanEntity plan = new BudgetPlanEntity();
        plan.setId(9L);
        plan.setUserId(1L);

        BpFundingLineEntity fundingLine = new BpFundingLineEntity();
        fundingLine.setId(42L);
        fundingLine.setBpBudgetId(3L);
        fundingLine.setBpIncomeId(7L);

        when(budgetPlanJpaRepository.findByIdAndUserId(9L, 1L)).thenReturn(Optional.of(plan));
        when(bpFundingLineJpaRepository.findAllByBudgetPlanId(9L)).thenReturn(List.of(fundingLine));

        adapter.deletePlan(1L, 9L);

        var order = inOrder(bpFundingLineJpaRepository, budgetPlanJpaRepository);
        order.verify(bpFundingLineJpaRepository).findAllByBudgetPlanId(9L);
        order.verify(bpFundingLineJpaRepository).deleteAll(List.of(fundingLine));
        order.verify(budgetPlanJpaRepository).delete(plan);
    }
}
