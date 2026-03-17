package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountTypeEntity;

import java.util.Optional;

public interface AccountTypeJpaRepository extends JpaRepository<AccountTypeEntity, Long> {
    Optional<AccountTypeEntity> findByDescription(String description);

    Boolean existsByDescription(String description);
}