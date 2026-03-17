package zachkingcade.dev.ledger.adapter.in.web.dto.account;

public record UpdateAccountResponse(
        Long accountId,
        Long typeId,
        String description,
        Boolean active,
        String notes
) { }
