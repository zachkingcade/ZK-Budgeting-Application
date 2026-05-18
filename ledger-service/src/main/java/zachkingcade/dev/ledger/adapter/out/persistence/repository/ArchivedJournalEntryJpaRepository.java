package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.ArchivedJournalEntryEntity;

import java.util.List;

public interface ArchivedJournalEntryJpaRepository extends JpaRepository<ArchivedJournalEntryEntity, Long> {

    @EntityGraph(attributePaths = {"journalLines"})
    List<ArchivedJournalEntryEntity> findAllByClosedAccountingPeriodIdAndUserIdOrderByEntryDateAscJournalEntryIdAsc(
            Long closedAccountingPeriodId,
            Long userId
    );
}
