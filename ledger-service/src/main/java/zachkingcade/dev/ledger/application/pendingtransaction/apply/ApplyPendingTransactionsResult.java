package zachkingcade.dev.ledger.application.pendingtransaction.apply;

import java.util.List;

public record ApplyPendingTransactionsResult(
        long successCount,
        long failureCount,
        List<Succeeded> succeeded,
        List<Failed> failed
) {
    public record Succeeded(
            Long pendingTransactionNumber,
            Long createdJournalEntryId
    ) {}

    public record Failed(
            Long pendingTransactionNumber,
            String message
    ) {}
}

