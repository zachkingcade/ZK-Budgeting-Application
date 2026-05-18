package zachkingcade.dev.ledger.application.port.out.journal;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.JournalEntryEntity;
import zachkingcade.dev.ledger.domain.journal.JournalEntry;
import zachkingcade.dev.ledger.domain.journal.JournalLine;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JournalEntryRepositoryPort {

    JournalEntry findById(Long userId, Long id);

    List<JournalEntry> findAll(Long userId);

    List<JournalEntry> findAll(Long userId, Sort sort);

    List<JournalEntry> findAll(Long userId, Specification<JournalEntryEntity> spec);

    List<JournalEntry> findAll(Long userId, Specification<JournalEntryEntity> spec, Sort sort);

    void removeJournalEntry(Long userId, Long id);

    JournalEntry save(JournalEntry journalEntryToSave);

    List<JournalLine> findLinesByAccountId(Long userId, Long accountId);

    /**
     * Live journal lines for an account. When both dates are empty, returns all live lines for the account.
     */
    List<JournalLine> findLiveLinesByAccountId(Long userId, Long accountId, Optional<LocalDate> entryDateFrom, Optional<LocalDate> entryDateTo);
}
