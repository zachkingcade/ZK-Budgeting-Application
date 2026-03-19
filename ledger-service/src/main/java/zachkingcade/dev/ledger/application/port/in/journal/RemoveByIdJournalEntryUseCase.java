package zachkingcade.dev.ledger.application.port.in.journal;

import zachkingcade.dev.ledger.application.commands.journal.RemoveByIdJournalEntryCommand;
import zachkingcade.dev.ledger.domain.journal.JournalEntry;

public interface RemoveByIdJournalEntryUseCase {
    public void removeJournalEntryById(RemoveByIdJournalEntryCommand command);
}
