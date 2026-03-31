package zachkingcade.dev.ledger.adapter.in.web.dto.accountclassification;

public record GetByIdAccountClassificationResponse(
        Long id,
        String description,
        char creditEffect,
        char debitEffect
) {
}
