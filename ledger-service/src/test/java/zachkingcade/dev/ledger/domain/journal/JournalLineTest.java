package zachkingcade.dev.ledger.domain.journal;

import org.junit.jupiter.api.Test;
import zachkingcade.dev.ledger.domain.exception.DomainException;

import static org.junit.jupiter.api.Assertions.*;

class JournalLineTest {

    @Test
    void createNew_amountNull_throwsDomainException() {
        assertThrows(DomainException.class, () -> JournalLine.createNew(null, 1L, 'D', "n"));
    }

    @Test
    void createNew_amountNonPositive_throwsDomainException() {
        assertThrows(DomainException.class, () -> JournalLine.createNew(0L, 1L, 'D', "n"));
        assertThrows(DomainException.class, () -> JournalLine.createNew(-5L, 1L, 'D', "n"));
    }

    @Test
    void createNew_directionInvalid_throwsDomainException() {
        assertThrows(DomainException.class, () -> JournalLine.createNew(5L, 1L, 'X', "n"));
    }

    @Test
    void createNew_accountIdNull_throwsDomainException() {
        assertThrows(DomainException.class, () -> JournalLine.createNew(5L, null, 'D', "n"));
    }

    @Test
    void createNew_validData_doesNotThrow() {
        JournalLine line = JournalLine.createNew(5L, 10L, 'C', "memo");
        assertEquals(5L, line.amount());
        assertEquals(10L, line.accountId());
        assertEquals('C', line.direction());
        assertEquals("memo", line.notes());
        assertNull(line.id());
    }
}

