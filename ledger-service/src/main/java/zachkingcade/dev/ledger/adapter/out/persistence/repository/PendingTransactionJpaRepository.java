package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.PendingTransactionEntity;

import java.util.List;
import java.util.Optional;

public interface PendingTransactionJpaRepository extends JpaRepository<PendingTransactionEntity, Long> {
    List<PendingTransactionEntity> findAllByUserIdOrderByTransactionDateDescTransactionNumberDesc(Long userId);

    Optional<PendingTransactionEntity> findByTransactionNumberAndUserId(Long transactionNumber, Long userId);
}

