package zachkingcade.dev.ledger.adapter.in.web.dto.journal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record JournalEntryFilters(
        Optional<LocalDate> dateAfter,
        Optional<LocalDate> dateBefore,
        Optional<String> descriptionContains,
        Optional<String> notesContains,
        Optional<List<Long>> accountTypes,
        Optional<List<Long>> accounts,
        Optional<String> searchContains
) {
}
