package zachkingcade.dev.ledger.application;

import org.junit.jupiter.api.Test;
import zachkingcade.dev.ledger.application.commands.journal.CreateJournalEntryCommand;
import zachkingcade.dev.ledger.application.commands.journal.JournalLineCommandObject;
import zachkingcade.dev.ledger.application.commands.journal.JournalLineUpdateCommandObject;
import zachkingcade.dev.ledger.application.commands.journal.UpdateJournalEntryCommand;
import zachkingcade.dev.ledger.application.port.out.journal.JournalEntryRepositoryPort;
import zachkingcade.dev.ledger.domain.journal.JournalEntry;
import zachkingcade.dev.ledger.domain.journal.JournalLine;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JournalEntryServiceTest {

    private static class FakeJournalEntryRepositoryPort implements JournalEntryRepositoryPort {
        private JournalEntry findByIdResult;
        private JournalEntry saved;
        private int saveCalls = 0;

        void whenFindById(JournalEntry entry) {
            this.findByIdResult = entry;
        }

        JournalEntry saved() {
            return saved;
        }

        int saveCalls() {
            return saveCalls;
        }

        @Override
        public JournalEntry findById(Long id) {
            return findByIdResult;
        }

        @Override
        public List<JournalEntry> findAll() {
            return List.of();
        }

        @Override
        public void removeJournalEntry(Long id) {
            return;
        }

        @Override
        public JournalEntry save(JournalEntry journalEntryToSave) {
            saveCalls++;
            this.saved = journalEntryToSave;
            return journalEntryToSave.withId(1L);
        }
    }

    private static JournalLine lineById(JournalEntry entry, Long id) {
        return entry.journalLines()
                .stream()
                .filter(l -> l.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void createJournalEntry_buildsJournalLinesAndSaves() {
        FakeJournalEntryRepositoryPort repo = new FakeJournalEntryRepositoryPort();
        JournalEntryService service = new JournalEntryService(repo);

        CreateJournalEntryCommand command = new CreateJournalEntryCommand(
                LocalDate.of(2026, 3, 19),
                "Rent",
                "entry-notes",
                List.of(
                        new JournalLineCommandObject(5L, 100L, 'C', "c-note"),
                        new JournalLineCommandObject(5L, 200L, 'D', "d-note")
                )
        );

        JournalEntry result = service.createJournalEntry(command);

        assertEquals(1L, result.id());
        assertEquals("Rent", result.description());
        assertEquals("entry-notes", result.notes());
        assertEquals(2, result.journalLines().size());

        // Validate conversion from command DTO -> domain objects
        JournalLine creditLine = result.journalLines().get(0);
        assertEquals(5L, creditLine.amount());
        assertEquals(100L, creditLine.accountId());
        assertEquals('C', creditLine.direction());
        assertEquals("c-note", creditLine.notes());

        JournalLine debitLine = result.journalLines().get(1);
        assertEquals(5L, debitLine.amount());
        assertEquals(200L, debitLine.accountId());
        assertEquals('D', debitLine.direction());
        assertEquals("d-note", debitLine.notes());
    }

    @Test
    void updateJournalEntry_updatesOnlyMatchingLineNotes() {
        FakeJournalEntryRepositoryPort repo = new FakeJournalEntryRepositoryPort();

        LocalDate date = LocalDate.of(2026, 3, 1);
        JournalLine line1 = JournalLine.rehydrate(10L, 5L, 100L, 'C', "old-line1");
        JournalLine line2 = JournalLine.rehydrate(11L, 5L, 200L, 'D', "old-line2");
        JournalEntry existing = JournalEntry.rehydrate(1L, date, "old-desc", "old-notes", List.of(line1, line2));

        repo.whenFindById(existing);

        JournalEntryService service = new JournalEntryService(repo);

        UpdateJournalEntryCommand command = new UpdateJournalEntryCommand(
                1L,
                Optional.empty(),
                Optional.empty(),
                List.of(new JournalLineUpdateCommandObject(10L, "new-line1"))
        );

        JournalEntry result = service.updateJournalEntry(command);

        assertEquals(1L, result.id());
        assertEquals("old-desc", result.description());
        assertEquals("old-notes", result.notes());

        JournalLine updatedLine1 = lineById(result, 10L);
        assertEquals("new-line1", updatedLine1.notes());
        assertEquals(5L, updatedLine1.amount());
        assertEquals(100L, updatedLine1.accountId());
        assertEquals('C', updatedLine1.direction());

        JournalLine untouchedLine2 = lineById(result, 11L);
        assertEquals("old-line2", untouchedLine2.notes());
        assertEquals(5L, untouchedLine2.amount());
        assertEquals(200L, untouchedLine2.accountId());
        assertEquals('D', untouchedLine2.direction());
    }
}

