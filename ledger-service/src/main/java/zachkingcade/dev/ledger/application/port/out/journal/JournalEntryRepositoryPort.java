package zachkingcade.dev.ledger.application.port.out.journal;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.JournalEntryEntity;
import zachkingcade.dev.ledger.domain.journal.JournalEntry;
import zachkingcade.dev.ledger.domain.journal.JournalLine;

import java.util.List;

public interface JournalEntryRepositoryPort {

    JournalEntry findById(Long userId, Long id);

    List<JournalEntry> findAll(Long userId);

    List<JournalEntry> findAll(Long userId, Sort sort);

    List<JournalEntry> findAll(Long userId, Specification<JournalEntryEntity> spec);

    List<JournalEntry> findAll(Long userId, Specification<JournalEntryEntity> spec, Sort sort);

    void removeJournalEntry(Long userId, Long id);

    JournalEntry save(JournalEntry journalEntryToSave);

    List<JournalLine> findLinesByAccountId(Long userId, Long accountId);
}
