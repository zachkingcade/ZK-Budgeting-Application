package zachkingcade.dev.ledger.adapter.in.web.dto.accountclassifcation;

public record AccountClassificationObject(
        Long id,
        String description,
        char creditEffect,
        char debitEffect
) {
}
