package zachkingcade.dev.ledger.adapter.out.persistence;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.ReplayableJournalEntryEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.ReplayableJournalEntryLineEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.ReplayableJournalEntryJpaRepository;
import zachkingcade.dev.ledger.application.port.out.replayablejournal.ReplayableJournalEntryRepositoryPort;
import zachkingcade.dev.ledger.domain.replayablejournal.ReplayableJournalEntry;
import zachkingcade.dev.ledger.domain.replayablejournal.ReplayableJournalEntryLine;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ReplayableJournalEntryPersistenceAdapter implements ReplayableJournalEntryRepositoryPort {

    private final ReplayableJournalEntryJpaRepository repository;

    public ReplayableJournalEntryPersistenceAdapter(ReplayableJournalEntryJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ReplayableJournalEntry> findAllByUserId(Long userId) {
        return repository.findAllByUserIdOrderByLastEditedAtDesc(userId).stream()
                .map(this::toDomainHeaderOnly)
                .toList();
    }

    @Override
    public Optional<ReplayableJournalEntry> findByIdAndUserId(Long id, Long userId) {
        return repository.findByIdAndUserId(id, userId).map(this::toDomainWithLines);
    }

    @Override
    public ReplayableJournalEntry save(ReplayableJournalEntry entry) {
        Instant now = Instant.now();
        ReplayableJournalEntryEntity entity;
        if (entry.id() != null) {
            entity = repository
                    .findByIdAndUserId(entry.id(), entry.userId())
                    .orElseThrow();
            entity.getLines().clear();
        } else {
            entity = new ReplayableJournalEntryEntity();
            entity.setUserId(entry.userId());
            entity.setCreatedAt(now);
        }

        entity.setReplayName(entry.replayName());
        entity.setReplayLineCount(entry.lines().size());
        entity.setLastEditedAt(now);

        List<ReplayableJournalEntryLineEntity> lineEntities = new ArrayList<>();
        List<ReplayableJournalEntryLine> sortedLines = entry.lines().stream()
                .sorted(Comparator.comparingInt(ReplayableJournalEntryLine::lineOrder))
                .toList();
        for (int i = 0; i < sortedLines.size(); i++) {
            ReplayableJournalEntryLine line = sortedLines.get(i);
            ReplayableJournalEntryLineEntity lineEntity = new ReplayableJournalEntryLineEntity();
            lineEntity.setReplayableJournalEntry(entity);
            lineEntity.setLineOrder(i);
            lineEntity.setAmount(line.amount());
            lineEntity.setAccountId(line.accountId());
            lineEntity.setDirection(line.direction());
            lineEntities.add(lineEntity);
        }
        entity.setLines(lineEntities);

        ReplayableJournalEntryEntity saved = repository.save(entity);
        return toDomainWithLines(saved);
    }

    @Override
    public void deleteByIdAndUserId(Long id, Long userId) {
        repository.deleteByIdAndUserId(id, userId);
    }

    private ReplayableJournalEntry toDomainHeaderOnly(ReplayableJournalEntryEntity entity) {
        return new ReplayableJournalEntry(
                entity.getId(),
                entity.getUserId(),
                entity.getReplayName(),
                entity.getReplayLineCount(),
                entity.getCreatedAt(),
                entity.getLastEditedAt(),
                List.of());
    }

    private ReplayableJournalEntry toDomainWithLines(ReplayableJournalEntryEntity entity) {
        List<ReplayableJournalEntryLine> lines = entity.getLines().stream()
                .sorted(Comparator.comparingInt(ReplayableJournalEntryLineEntity::getLineOrder))
                .map(le -> new ReplayableJournalEntryLine(
                        le.getId(),
                        le.getLineOrder(),
                        le.getAmount(),
                        le.getAccountId(),
                        le.getDirection()))
                .toList();
        return new ReplayableJournalEntry(
                entity.getId(),
                entity.getUserId(),
                entity.getReplayName(),
                entity.getReplayLineCount(),
                entity.getCreatedAt(),
                entity.getLastEditedAt(),
                lines);
    }
}
