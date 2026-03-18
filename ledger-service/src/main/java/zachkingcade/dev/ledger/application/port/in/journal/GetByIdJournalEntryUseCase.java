package zachkingcade.dev.ledger.application.port.in.journal;

import zachkingcade.dev.ledger.application.commands.journal.GetByIdJournalEntryCommand;
import zachkingcade.dev.ledger.domain.journal.JournalEntry;

public interface GetByIdJournalEntryUseCase {
    JournalEntry getByIdJournalEntry(GetByIdJournalEntryCommand command);
}
