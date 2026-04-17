package zachkingcade.dev.ledger.application.commands.journal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record JournalEntryFilterCommandObject(
        Optional<LocalDate> dateAfter,
        Optional<LocalDate> dateBefore,
        Optional<String> descriptionContains,
        Optional<String> notesContains,
        Optional<List<Long>> accountTypes,
        Optional<List<Long>> accounts,
        Optional<String> searchContains
) {
}
