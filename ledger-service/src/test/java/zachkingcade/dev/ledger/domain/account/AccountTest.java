package zachkingcade.dev.ledger.domain.account;

import org.junit.jupiter.api.Test;
import zachkingcade.dev.ledger.domain.exception.DomainException;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void createNew_typeIdNull_throwsDomainException() {
        assertThrows(DomainException.class, () -> Account.createNew(null, "desc", "notes", 1L));
    }

    @Test
    void createNew_descriptionEmpty_throwsDomainException() {
        assertThrows(DomainException.class, () -> Account.createNew(1L, "", "notes", 1L));
    }

    @Test
    void createNew_notesNull_defaultsToEmptyString() {
        Account account = Account.createNew(1L, "desc", null, 1L);
        assertEquals(1L, account.typeId());
        assertEquals("desc", account.description());
        assertTrue(account.active());
        assertEquals("", account.notes());
    }
}

