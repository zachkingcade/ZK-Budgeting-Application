package zachkingcade.dev.ledger.application.port.in.journal;

import zachkingcade.dev.ledger.application.commands.journal.CreateJournalEntryCommand;
import zachkingcade.dev.ledger.domain.journal.JournalEntry;
import zachkingcade.dev.ledger.domain.journal.JournalLine;

import java.sql.Date;
import java.util.List;

public interface CreateJournalEntryUseCase {
    JournalEntry createJournalEntry(CreateJournalEntryCommand command);
}
