package zachkingcade.dev.ledger.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.exception.NotFoundException;
import zachkingcade.dev.ledger.application.port.out.replayablejournal.ReplayableJournalEntryRepositoryPort;
import zachkingcade.dev.ledger.application.replayablejournal.ReplayableJournalEntryViews.ReplayableJournalEntryLineView;
import zachkingcade.dev.ledger.domain.replayablejournal.ReplayableJournalEntry;
import zachkingcade.dev.ledger.domain.replayablejournal.ReplayableJournalEntryLine;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplayableJournalEntryServiceTest {

    @Mock
    private ReplayableJournalEntryRepositoryPort repository;

    @InjectMocks
    private ReplayableJournalEntryService service;

    private static List<ReplayableJournalEntryLineView> balancedLines() {
        return List.of(
                new ReplayableJournalEntryLineView(100L, 1L, 'D'),
                new ReplayableJournalEntryLineView(100L, 2L, 'C'));
    }

    @Test
    void createRejectsUnbalancedLines() {
        List<ReplayableJournalEntryLineView> lines = List.of(
                new ReplayableJournalEntryLineView(100L, 1L, 'D'),
                new ReplayableJournalEntryLineView(50L, 2L, 'C'));

        assertThrows(ApplicationException.class, () -> service.create(1L, "Rent", lines, true));
    }

    @Test
    void createAllowsSingleDebitLineWhenNotRequiringBalance() {
        when(repository.save(any())).thenAnswer(inv -> {
            ReplayableJournalEntry e = inv.getArgument(0);
            return new ReplayableJournalEntry(
                    3L, e.userId(), e.replayName(), e.replayLineCount(), Instant.now(), Instant.now(), e.lines());
        });

        var created = service.create(
                1L,
                "Paycheck",
                List.of(new ReplayableJournalEntryLineView(5000L, 10L, 'D')),
                false);

        assertEquals(1, created.lines().size());
    }

    @Test
    void createPersistsBalancedTemplate() {
        when(repository.save(any())).thenAnswer(inv -> {
            ReplayableJournalEntry e = inv.getArgument(0);
            return new ReplayableJournalEntry(
                    9L, e.userId(), e.replayName(), e.replayLineCount(), Instant.now(), Instant.now(), e.lines());
        });

        var created = service.create(1L, "Rent", balancedLines(), true);

        assertEquals(9L, created.replayableJournalEntryId());
        assertEquals(2, created.lines().size());
        verify(repository).save(any());
    }

    @Test
    void getByIdNotFoundThrows() {
        when(repository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(1L, 5L));
    }

    @Test
    void updateRequiresExistingEntry() {
        when(repository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.update(1L, 5L, "Rent", balancedLines(), true));
    }
}
