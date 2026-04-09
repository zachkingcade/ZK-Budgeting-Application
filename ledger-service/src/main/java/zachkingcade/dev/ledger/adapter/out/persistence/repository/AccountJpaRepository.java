package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;

import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long>, JpaSpecificationExecutor<AccountEntity> {

    Optional<AccountEntity> findByIdAndUserId(Long id, Long userId);

    Boolean existsByDescriptionAndUserId(String description, Long userId);

    Optional<AccountEntity> findByDescriptionAndUserId(String description, Long userId);
}
