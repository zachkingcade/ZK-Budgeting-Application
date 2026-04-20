package zachkingcade.dev.ledger.domain.pendingtransaction;

import java.time.LocalDate;

public record PendingTransaction(
        Long transactionNumber,
        Long userId,
        LocalDate transactionDate,
        String description,
        Long amount,
        String notes
) {}

