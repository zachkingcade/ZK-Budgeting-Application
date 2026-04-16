package zachkingcade.dev.ledger.domain.journal;

import org.junit.jupiter.api.Test;
import zachkingcade.dev.ledger.domain.exception.DomainException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JournalEntryTest {

    @Test
    void createNew_entryDateNull_throwsDomainException() {
        List<JournalLine> lines = List.of(
                JournalLine.createNew(5L, 1L, 'C', null),
                JournalLine.createNew(5L, 2L, 'D', null)
        );

        assertThrows(DomainException.class, () -> JournalEntry.createNew(null, "desc", null, 1L, lines));
    }

    @Test
    void createNew_descriptionEmpty_throwsDomainException() {
        List<JournalLine> lines = List.of(
                JournalLine.createNew(5L, 1L, 'C', null),
                JournalLine.createNew(5L, 2L, 'D', null)
        );

        assertThrows(DomainException.class, () -> JournalEntry.createNew(LocalDate.now(), "", null, 1L, lines));
    }

    @Test
    void createNew_fewerThanTwoLines_throwsDomainException() {
        List<JournalLine> lines = List.of(
                JournalLine.createNew(5L, 1L, 'C', null)
        );

        assertThrows(DomainException.class, () -> JournalEntry.createNew(LocalDate.now(), "desc", null, 1L, lines));
    }

    @Test
    void createNew_creditDebitMismatch_throwsDomainException() {
        List<JournalLine> lines = List.of(
                JournalLine.createNew(5L, 1L, 'C', null),
                JournalLine.createNew(4L, 2L, 'D', null)
        );

        assertThrows(DomainException.class, () -> JournalEntry.createNew(LocalDate.now(), "desc", null, 1L, lines));
    }

    @Test
    void createNew_validData_doesNotThrow() {
        List<JournalLine> lines = List.of(
                JournalLine.createNew(5L, 1L, 'C', "c-note"),
                JournalLine.createNew(5L, 2L, 'D', "d-note")
        );

        JournalEntry entry = JournalEntry.createNew(LocalDate.now(), "desc", "notes", 1L, lines);
        assertNull(entry.id());
        assertEquals("desc", entry.description());
        assertEquals("notes", entry.notes());
        assertEquals(2, entry.journalLines().size());
    }
}

