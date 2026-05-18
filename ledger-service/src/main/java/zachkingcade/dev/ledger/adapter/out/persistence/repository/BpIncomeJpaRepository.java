package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.BpIncomeEntity;

import java.util.List;
import java.util.Optional;

public interface BpIncomeJpaRepository extends JpaRepository<BpIncomeEntity, Long> {

    List<BpIncomeEntity> findAllByBudgetPlanIdOrderByIdAsc(Long budgetPlanId);

    Optional<BpIncomeEntity> findByIdAndBudgetPlanId(Long id, Long budgetPlanId);
}
