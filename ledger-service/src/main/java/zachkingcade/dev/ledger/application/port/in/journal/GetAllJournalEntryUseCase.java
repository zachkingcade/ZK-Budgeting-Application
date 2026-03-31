package zachkingcade.dev.ledger.application.port.in.journal;

import zachkingcade.dev.ledger.application.commands.journal.GetAllJournalEntriesCommand;
import zachkingcade.dev.ledger.domain.journal.JournalEntry;

import java.util.List;

public interface GetAllJournalEntryUseCase {
    List<JournalEntry> getAllJournalEntries(GetAllJournalEntriesCommand command);
}
