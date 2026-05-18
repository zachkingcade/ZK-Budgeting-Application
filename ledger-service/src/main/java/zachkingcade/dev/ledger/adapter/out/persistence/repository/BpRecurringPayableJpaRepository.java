package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.BpRecurringPayableEntity;

import java.util.List;
import java.util.Optional;

public interface BpRecurringPayableJpaRepository extends JpaRepository<BpRecurringPayableEntity, Long> {

    List<BpRecurringPayableEntity> findAllByBudgetPlanIdOrderByIdAsc(Long budgetPlanId);

    Optional<BpRecurringPayableEntity> findByIdAndBudgetPlanId(Long id, Long budgetPlanId);
}
