package zachkingcade.dev.ledger.adapter.in.web.dto.accountclassifcation;

public record GetByIdAccountClassificationResponse(
        Long id,
        String description,
        char creditEffect,
        char debitEffect
) {
}
