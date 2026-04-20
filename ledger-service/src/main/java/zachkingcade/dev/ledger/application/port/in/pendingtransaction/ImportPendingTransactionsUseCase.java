package zachkingcade.dev.ledger.application.port.in.pendingtransaction;

import zachkingcade.dev.ledger.application.pendingtransaction.ImportPendingTransactionsResult;

import java.io.InputStream;

public interface ImportPendingTransactionsUseCase {
    ImportPendingTransactionsResult importPendingTransactions(Long userId, Long formatId, InputStream inputStream);
}

