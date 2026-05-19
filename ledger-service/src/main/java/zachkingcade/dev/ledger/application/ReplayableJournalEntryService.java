package zachkingcade.dev.ledger.application;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.exception.NotFoundException;
import zachkingcade.dev.ledger.application.port.in.replayablejournal.ReplayableJournalEntryUseCase;
import zachkingcade.dev.ledger.application.port.out.replayablejournal.ReplayableJournalEntryRepositoryPort;
import zachkingcade.dev.ledger.application.replayablejournal.ReplayableJournalEntryViews.ReplayableJournalEntryDetailView;
import zachkingcade.dev.ledger.application.replayablejournal.ReplayableJournalEntryViews.ReplayableJournalEntryListItem;
import zachkingcade.dev.ledger.application.replayablejournal.ReplayableJournalEntryViews.ReplayableJournalEntryLineView;
import zachkingcade.dev.ledger.domain.replayablejournal.ReplayableJournalEntry;
import zachkingcade.dev.ledger.domain.replayablejournal.ReplayableJournalEntryLine;
import zachkingcade.dev.ledger.domain.replayablejournal.ReplayableJournalEntryValidator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReplayableJournalEntryService implements ReplayableJournalEntryUseCase {

    private final ReplayableJournalEntryRepositoryPort repository;

    public ReplayableJournalEntryService(ReplayableJournalEntryRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<ReplayableJournalEntryListItem> list(Long userId) {
        return repository.findAllByUserId(userId).stream().map(this::toListItem).toList();
    }

    @Override
    public ReplayableJournalEntryDetailView getById(Long userId, Long id) {
        return toDetail(requireEntry(userId, id));
    }

    @Override
    @Transactional
    public ReplayableJournalEntryDetailView create(
            Long userId, String replayName, List<ReplayableJournalEntryLineView> lines, boolean requireBalanced) {
        ReplayableJournalEntryValidator.validateName(replayName);
        List<ReplayableJournalEntryLine> domainLines = toDomainLines(lines);
        ReplayableJournalEntryValidator.validateLines(domainLines, requireBalanced);

        ReplayableJournalEntry toSave = new ReplayableJournalEntry(
                null, userId, replayName.trim(), domainLines.size(), null, null, domainLines);
        try {
            return toDetail(repository.save(toSave));
        } catch (DataIntegrityViolationException ex) {
            throw duplicateNameException(ex);
        }
    }

    @Override
    @Transactional
    public ReplayableJournalEntryDetailView update(
            Long userId, Long id, String replayName, List<ReplayableJournalEntryLineView> lines, boolean requireBalanced) {
        requireEntry(userId, id);
        ReplayableJournalEntryValidator.validateName(replayName);
        List<ReplayableJournalEntryLine> domainLines = toDomainLines(lines);
        ReplayableJournalEntryValidator.validateLines(domainLines, requireBalanced);

        ReplayableJournalEntry existing = repository.findByIdAndUserId(id, userId).orElseThrow();
        ReplayableJournalEntry toSave = new ReplayableJournalEntry(
                id,
                userId,
                replayName.trim(),
                domainLines.size(),
                existing.createdAt(),
                Instant.now(),
                domainLines);
        try {
            return toDetail(repository.save(toSave));
        } catch (DataIntegrityViolationException ex) {
            throw duplicateNameException(ex);
        }
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        if (repository.findByIdAndUserId(id, userId).isEmpty()) {
            throw new NotFoundException("Replayable journal entry not found");
        }
        repository.deleteByIdAndUserId(id, userId);
    }

    private ReplayableJournalEntry requireEntry(Long userId, Long id) {
        return repository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Replayable journal entry not found"));
    }

    private static List<ReplayableJournalEntryLine> toDomainLines(List<ReplayableJournalEntryLineView> lines) {
        if (lines == null || lines.isEmpty()) {
            return List.of();
        }
        List<ReplayableJournalEntryLine> result = new ArrayList<>();
        int order = 0;
        for (ReplayableJournalEntryLineView line : lines) {
            if (line == null) {
                continue;
            }
            result.add(new ReplayableJournalEntryLine(null, order++, line.amount(), line.accountId(), line.direction()));
        }
        return result;
    }

    private ReplayableJournalEntryListItem toListItem(ReplayableJournalEntry entry) {
        return new ReplayableJournalEntryListItem(
                entry.id(),
                entry.replayName(),
                entry.replayLineCount(),
                entry.createdAt(),
                entry.lastEditedAt());
    }

    private ReplayableJournalEntryDetailView toDetail(ReplayableJournalEntry entry) {
        List<ReplayableJournalEntryLineView> lines = entry.lines().stream()
                .map(l -> new ReplayableJournalEntryLineView(l.amount(), l.accountId(), l.direction()))
                .toList();
        return new ReplayableJournalEntryDetailView(
                entry.id(),
                entry.replayName(),
                entry.replayLineCount(),
                entry.createdAt(),
                entry.lastEditedAt(),
                lines);
    }

    private static ApplicationException duplicateNameException(DataIntegrityViolationException ex) {
        String root = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        String r = root == null ? "" : root.toLowerCase();
        if (r.contains("uq_replayable_journal_entries_user_name") || r.contains("replay_name")) {
            return new ApplicationException("A replayable journal entry with this name already exists.");
        }
        return new ApplicationException("Could not save replayable journal entry.");
    }
}
