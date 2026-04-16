package zachkingcade.dev.ledger.domain.account;

import org.junit.jupiter.api.Test;
import zachkingcade.dev.ledger.domain.exception.DomainException;

import static org.junit.jupiter.api.Assertions.*;

class AccountTypeTest {

    @Test
    void createNew_descriptionEmpty_throwsDomainException() {
        assertThrows(DomainException.class, () -> AccountType.createNew("", 1L, "notes", 1L, false));
    }

    @Test
    void createNew_classificationIdNull_throwsDomainException() {
        assertThrows(DomainException.class, () -> AccountType.createNew("desc", null, "notes", 1L, false));
    }

    @Test
    void createNew_validData_doesNotThrow() {
        AccountType accountType = AccountType.createNew("desc", 99L, null, 1L, false);
        assertNull(accountType.id());
        assertEquals("desc", accountType.description());
        assertEquals(99L, accountType.classificationId());
        assertTrue(accountType.active());
        assertNull(accountType.notes());
    }
}

