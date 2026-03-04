package zachkingcade.dev.ledger.adapter.in.web.dto;

public record CreateAccountResponse(
        Long accountId,
        Long typeId,
        String description,
        boolean active,
        String notes
) { }
