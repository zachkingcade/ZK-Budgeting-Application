package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.BpFundingLineEntity;

import java.util.List;
import java.util.Optional;

public interface BpFundingLineJpaRepository extends JpaRepository<BpFundingLineEntity, Long> {

    List<BpFundingLineEntity> findAllByBpBudgetIdOrderByIdAsc(Long bpBudgetId);

    Optional<BpFundingLineEntity> findByIdAndBpBudgetId(Long id, Long bpBudgetId);

    List<BpFundingLineEntity> findAllByBpIncomeId(Long bpIncomeId);

    @Query(
            """
                    SELECT f FROM BpFundingLineEntity f, BpBudgetEntity b
                    WHERE f.bpBudgetId = b.id AND b.budgetPlanId = :planId
                    """)
    List<BpFundingLineEntity> findAllByBudgetPlanId(@Param("planId") Long planId);
}
