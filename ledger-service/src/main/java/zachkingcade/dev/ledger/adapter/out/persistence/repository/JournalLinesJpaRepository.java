package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.JournalEntryEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.JournalLineEntity;

import java.util.List;
import java.util.Optional;

public interface JournalLinesJpaRepository extends JpaRepository<JournalLineEntity, Long> {
    List<JournalLineEntity> findByJournalEntry(JournalEntryEntity entity);

    List<JournalLineEntity> findByAccountId(Long accountId);

    List<JournalLineEntity> findByAccountIdAndJournalEntryUserId(Long accountId, Long userId);
}
