package zachkingcade.dev.ledger.application.port.out.journal;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.JournalEntryEntity;
import zachkingcade.dev.ledger.domain.journal.JournalEntry;

import java.util.List;

public interface JournalEntryRepositoryPort {

    public JournalEntry findById(Long id);

    List<JournalEntry> findAll();

    List<JournalEntry> findAll(Sort sort);

    List<JournalEntry> findAll(Specification<JournalEntryEntity> spec);

    List<JournalEntry> findAll(Specification<JournalEntryEntity> spec, Sort sort);

    void removeJournalEntry(Long id);

    JournalEntry save(JournalEntry journalEntryToSave);
}
