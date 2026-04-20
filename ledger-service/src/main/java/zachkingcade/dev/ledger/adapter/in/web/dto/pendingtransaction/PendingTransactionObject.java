package zachkingcade.dev.ledger.adapter.in.web.dto.pendingtransaction;

public record PendingTransactionObject(
        Long transactionNumber,
        String transactionDate,
        String description,
        Long amount,
        String notes
) {}

