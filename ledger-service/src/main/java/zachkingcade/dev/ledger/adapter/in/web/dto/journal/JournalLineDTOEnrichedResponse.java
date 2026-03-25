package zachkingcade.dev.ledger.adapter.in.web.dto.journal;

public record JournalLineDTOEnrichedResponse(
        Long id,
        Long amount,
        Long accountId,
        String accountName,
        String accountTypeName,
        String accountDisplayName,
        char lineAffectOnAccount,
        char direction,
        String notes
) {
}
