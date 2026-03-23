package zachkingcade.dev.ledger.application.port.in.journal;

import zachkingcade.dev.ledger.application.commands.journal.GetAllJournalEntrysCommand;
import zachkingcade.dev.ledger.domain.journal.JournalEntry;

import java.util.List;

public interface GetAllJournalEntryUsecase {
    List<JournalEntry> getAllJournalEntries(GetAllJournalEntrysCommand command);
}
