package zachkingcade.dev.ledger.application.commands.journal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record CreateJournalEntryCommand(
        LocalDate entryDate,
        String description,
        Optional<String> notes,
        List<JournalLineCommandObject> journalLinesList
) {
}
