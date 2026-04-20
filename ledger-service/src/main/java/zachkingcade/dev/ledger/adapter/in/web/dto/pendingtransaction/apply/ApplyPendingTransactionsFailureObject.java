package zachkingcade.dev.ledger.adapter.in.web.dto.pendingtransaction.apply;

public record ApplyPendingTransactionsFailureObject(
        Long pendingTransactionNumber,
        String message
) {}

