package zachkingcade.dev.ledger.application.commands.journal;

import java.util.List;
import java.util.Optional;

public record UpdateJournalEntryCommand(
        Long userId,
        Long id,
        Optional<String> description,
        Optional<String> notes,
        List<JournalLineUpdateCommandObject> journalLinesList
) {
}
