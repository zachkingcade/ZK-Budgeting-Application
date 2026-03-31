package zachkingcade.dev.ledger.application.commands.journal;

import zachkingcade.dev.ledger.application.commands.shared.SortObjectCommandObject;
import zachkingcade.dev.ledger.application.validation.JournalEntrySortType;

import java.util.Optional;

public record GetAllJournalEntriesCommand(
        Optional<SortObjectCommandObject<JournalEntrySortType>> sort,
        Optional<JournalEntryFilterCommandObject> filters
) { }
