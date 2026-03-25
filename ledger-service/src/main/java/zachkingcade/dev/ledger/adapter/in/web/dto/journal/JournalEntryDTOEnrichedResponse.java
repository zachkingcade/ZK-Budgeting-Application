package zachkingcade.dev.ledger.adapter.in.web.dto.journal;

import java.time.LocalDate;
import java.util.List;

public record JournalEntryDTOEnrichedResponse(
        Long id,
        LocalDate entryDate,
        String description,
        String notes,
        List<JournalLineDTOEnrichedResponse> journalLines
) {
}
