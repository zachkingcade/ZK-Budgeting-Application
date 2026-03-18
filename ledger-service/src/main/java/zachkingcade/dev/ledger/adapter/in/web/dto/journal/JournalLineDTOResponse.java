package zachkingcade.dev.ledger.adapter.in.web.dto.journal;

public record JournalLineDTOResponse(
        Long id,
        Long amount,
        Long accountId,
        char direction,
        String notes
) {
}
