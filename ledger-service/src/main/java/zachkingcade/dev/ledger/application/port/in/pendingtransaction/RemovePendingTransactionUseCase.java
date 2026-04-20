package zachkingcade.dev.ledger.application.port.in.pendingtransaction;

public interface RemovePendingTransactionUseCase {
    void removePendingTransaction(Long userId, Long transactionNumber);
}

