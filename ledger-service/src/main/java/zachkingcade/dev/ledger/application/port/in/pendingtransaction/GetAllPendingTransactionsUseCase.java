package zachkingcade.dev.ledger.application.port.in.pendingtransaction;

import zachkingcade.dev.ledger.domain.pendingtransaction.PendingTransaction;

import java.util.List;

public interface GetAllPendingTransactionsUseCase {
    List<PendingTransaction> getAllPendingTransactionsForUser(Long userId);
}

