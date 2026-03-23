package zachkingcade.dev.ledger.adapter.in.web.dto.journal;

import java.util.Optional;

public record JournalLineDTORequest(
        Long amount,
        Long accountId,
        char direction,
        Optional<String> notes
) {
}
