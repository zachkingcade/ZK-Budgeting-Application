package zachkingcade.dev.ledger.application.commands.journal;

import java.time.LocalDate;
import java.util.List;

public record CreateJournalEntryCommand(
        LocalDate entryDate,
        String description,
        String notes,
        List<JournalLineCommandObject> journalLinesList
) {
}
