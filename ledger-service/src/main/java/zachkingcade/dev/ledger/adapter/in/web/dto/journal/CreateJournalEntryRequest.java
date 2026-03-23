package zachkingcade.dev.ledger.adapter.in.web.dto.journal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record CreateJournalEntryRequest(
        LocalDate entryDate,
        String description,
        Optional<String> notes,
        List<JournalLineDTORequest> journalLines
) {
}
