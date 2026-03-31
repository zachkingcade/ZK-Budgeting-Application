package zachkingcade.dev.ledger.application.port.in.journal;

import zachkingcade.dev.ledger.application.commands.journal.UpdateJournalEntryCommand;
import zachkingcade.dev.ledger.domain.journal.JournalEntry;

public interface UpdateJournalEntryUseCase {
    JournalEntry updateJournalEntry(UpdateJournalEntryCommand command);
}
