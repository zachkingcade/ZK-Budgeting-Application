package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.BpBudgetEntity;

import java.util.List;
import java.util.Optional;

public interface BpBudgetJpaRepository extends JpaRepository<BpBudgetEntity, Long> {

    List<BpBudgetEntity> findAllByBudgetPlanIdOrderByIdAsc(Long budgetPlanId);

    Optional<BpBudgetEntity> findByIdAndBudgetPlanId(Long id, Long budgetPlanId);
}
