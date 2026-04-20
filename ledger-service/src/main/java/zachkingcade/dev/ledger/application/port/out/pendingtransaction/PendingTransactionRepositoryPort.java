package zachkingcade.dev.ledger.application.port.out.pendingtransaction;

import zachkingcade.dev.ledger.domain.pendingtransaction.PendingTransaction;

import java.util.List;
import java.util.Optional;

public interface PendingTransactionRepositoryPort {
    List<PendingTransaction> findAllByUserId(Long userId);

    Optional<PendingTransaction> findByTransactionNumberAndUserId(Long transactionNumber, Long userId);

    void deleteByTransactionNumber(Long transactionNumber);

    PendingTransaction save(PendingTransaction pendingTransaction);
}

