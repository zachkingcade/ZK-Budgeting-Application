package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.BudgetPlanEntity;

import java.util.List;
import java.util.Optional;

public interface BudgetPlanJpaRepository extends JpaRepository<BudgetPlanEntity, Long> {

    List<BudgetPlanEntity> findAllByUserIdOrderByLastEditedAtDesc(Long userId);

    Optional<BudgetPlanEntity> findByIdAndUserId(Long id, Long userId);
}
