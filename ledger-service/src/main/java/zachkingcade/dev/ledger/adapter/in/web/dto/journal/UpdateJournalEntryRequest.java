package zachkingcade.dev.ledger.adapter.in.web.dto.journal;

import java.util.List;
import java.util.Optional;

public record UpdateJournalEntryRequest(
        Long id,
        Optional<String> description,
        /** Null if omitted (leave unchanged); empty string clears entry notes. */
        String notes,
        List<JournalLineDTOUpdate> journalLines
) {
}
