package zachkingcade.dev.ledger.application.port.in.pendingtransaction;

import zachkingcade.dev.ledger.application.pendingtransaction.apply.ApplyPendingTransactionsCommand;
import zachkingcade.dev.ledger.application.pendingtransaction.apply.ApplyPendingTransactionsResult;

public interface ApplyPendingTransactionsUseCase {
    ApplyPendingTransactionsResult applyPendingTransactions(ApplyPendingTransactionsCommand command);
}

