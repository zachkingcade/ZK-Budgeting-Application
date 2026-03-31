package zachkingcade.dev.ledger.adapter.in.web.dto.accountclassification;

public record AccountClassificationObject(
        Long id,
        String description,
        char creditEffect,
        char debitEffect
) {
}
