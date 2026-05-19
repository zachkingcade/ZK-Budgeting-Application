package zachkingcade.dev.ledger.application.port.out.replayablejournal;

import zachkingcade.dev.ledger.domain.replayablejournal.ReplayableJournalEntry;

import java.util.List;
import java.util.Optional;

public interface ReplayableJournalEntryRepositoryPort {

    List<ReplayableJournalEntry> findAllByUserId(Long userId);

    Optional<ReplayableJournalEntry> findByIdAndUserId(Long id, Long userId);

    ReplayableJournalEntry save(ReplayableJournalEntry entry);

    void deleteByIdAndUserId(Long id, Long userId);
}
