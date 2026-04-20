package zachkingcade.dev.ledger.application.importtypes;

import java.time.LocalDate;

public record PendingTransactionDraft(
        LocalDate transactionDate,
        String description,
        Long amountMinorUnits,
        String notes
) {}

