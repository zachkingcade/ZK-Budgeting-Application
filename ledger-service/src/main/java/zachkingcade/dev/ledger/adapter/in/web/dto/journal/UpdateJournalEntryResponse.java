package zachkingcade.dev.ledger.adapter.in.web.dto.journal;

import java.time.LocalDate;
import java.util.List;

public record UpdateJournalEntryResponse(
        Long id,
        LocalDate entryDate,
        String description,
        String notes,
        List<JournalLineDTOResponse> journalLines
) {
}
