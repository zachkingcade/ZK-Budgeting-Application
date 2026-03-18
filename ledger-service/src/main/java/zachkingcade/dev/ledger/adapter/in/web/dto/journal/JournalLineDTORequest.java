package zachkingcade.dev.ledger.adapter.in.web.dto.journal;

public record JournalLineDTORequest(
        Long amount,
        Long accountId,
        char direction,
        String notes
) {
}
