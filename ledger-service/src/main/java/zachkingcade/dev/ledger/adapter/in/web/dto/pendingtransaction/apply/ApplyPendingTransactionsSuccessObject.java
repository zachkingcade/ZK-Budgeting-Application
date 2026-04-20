package zachkingcade.dev.ledger.adapter.in.web.dto.pendingtransaction.apply;

public record ApplyPendingTransactionsSuccessObject(
        Long pendingTransactionNumber,
        Long createdJournalEntryId
) {}

