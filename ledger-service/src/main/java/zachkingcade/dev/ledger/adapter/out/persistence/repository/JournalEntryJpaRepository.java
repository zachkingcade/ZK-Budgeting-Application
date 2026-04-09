package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.JournalEntryEntity;

import java.util.List;
import java.util.Optional;

public interface JournalEntryJpaRepository extends JpaRepository<JournalEntryEntity, Long>, JpaSpecificationExecutor<JournalEntryEntity> {

    @EntityGraph(attributePaths = {"journalLines", "journalLines.account"})
    Optional<JournalEntryEntity> findWithJournalLinesById(Long id);

    @EntityGraph(attributePaths = {"journalLines", "journalLines.account"})
    Optional<JournalEntryEntity> findWithJournalLinesByIdAndUserId(Long id, Long userId);

    @Override
    @EntityGraph(attributePaths = {"journalLines", "journalLines.account"})
    List<JournalEntryEntity> findAll();

    @EntityGraph(attributePaths = {"journalLines", "journalLines.account"})
    List<JournalEntryEntity> findAllByUserId(Long userId);
}
