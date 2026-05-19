package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.ReplayableJournalEntryEntity;

import java.util.List;
import java.util.Optional;

public interface ReplayableJournalEntryJpaRepository extends JpaRepository<ReplayableJournalEntryEntity, Long> {

    List<ReplayableJournalEntryEntity> findAllByUserIdOrderByLastEditedAtDesc(Long userId);

    @EntityGraph(attributePaths = "lines")
    Optional<ReplayableJournalEntryEntity> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}
