package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountClassificationEntity;

public interface AccountClassificationJpaRepository extends JpaRepository<AccountClassificationEntity, Long> {
}
