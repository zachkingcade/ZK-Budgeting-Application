package zachkingcade.dev.ledger.adapter.in.web.dto.accounttype;

public record UpdateAccountTypeResponse(
        Long id,
        Long classificationId,
        String description,
        boolean active,
        String notes
) {
}
