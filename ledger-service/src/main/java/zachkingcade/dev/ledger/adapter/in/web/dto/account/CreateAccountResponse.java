package zachkingcade.dev.ledger.adapter.in.web.dto.account;

public record CreateAccountResponse(
        Long accountId,
        Long typeId,
        String description,
        boolean active,
        String notes
) { }
