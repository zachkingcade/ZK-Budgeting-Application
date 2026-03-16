package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {

    Boolean existsByDescription(String description);

    AccountEntity findByDescription(String description);
}
