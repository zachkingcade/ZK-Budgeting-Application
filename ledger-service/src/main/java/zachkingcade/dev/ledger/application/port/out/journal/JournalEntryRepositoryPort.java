package zachkingcade.dev.ledger.application.port.out.journal;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.domain.journal.JournalEntry;

import java.util.List;

public interface JournalEntryRepositoryPort {

    public JournalEntry findById(Long id);

    List<JournalEntry> findAll();

    void removeJournalEntry(Long id);

    JournalEntry save(JournalEntry journalEntryToSave);
}
