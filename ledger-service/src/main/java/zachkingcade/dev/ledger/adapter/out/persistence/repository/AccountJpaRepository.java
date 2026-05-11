package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;

import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long>, JpaSpecificationExecutor<AccountEntity> {

    Optional<AccountEntity> findByIdAndUserId(Long id, Long userId);

    Boolean existsByDescriptionAndUserIdAndType_Id(String description, Long userId, Long typeId);

    Optional<AccountEntity> findByDescriptionAndUserIdAndType_Id(String description, Long userId, Long typeId);
}
