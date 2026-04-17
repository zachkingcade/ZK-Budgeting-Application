package zachkingcade.dev.ledger.adapter.in.web.dto.account;

public record GetAccountByIdResponse(
        Long accountId,
        Long typeId,
        String description,
        String accountTypeName,
        String accountDisplayName,
        Long accountBalance,
        boolean active,
        String notes,
        String creditEffect,
        String debitEffect
) {
}
