package zachkingcade.dev.ledger.application.port.in.journal;

import zachkingcade.dev.ledger.application.commands.journal.UpdateJournalEntryCommand;
import zachkingcade.dev.ledger.domain.journal.JournalEntry;
import zachkingcade.dev.ledger.domain.journal.JournalLine;

import java.time.LocalDate;
import java.util.List;

public interface UpdateJournalEntryUsecase {
    JournalEntry updateJournalEntry(UpdateJournalEntryCommand command);
}
