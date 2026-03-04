package zachkingcade.dev.ledger.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {
}
