package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountTypeEntity;

import java.util.List;
import java.util.Optional;

public interface AccountTypeJpaRepository extends JpaRepository<AccountTypeEntity, Long>, JpaSpecificationExecutor<AccountTypeEntity> {
    Optional<AccountTypeEntity> findByDescription(String description);

    Boolean existsByDescription(String description);

    Optional<AccountTypeEntity> findByIdAndUserId(Long id, Long userId);

    Optional<AccountTypeEntity> findByIdAndSystemAccountTrue(Long id);

    List<AccountTypeEntity> findAllByUserIdOrSystemAccountTrue(Long userId);
}