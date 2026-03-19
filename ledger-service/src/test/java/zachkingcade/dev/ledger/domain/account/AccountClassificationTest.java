package zachkingcade.dev.ledger.domain.account;

import org.junit.jupiter.api.Test;
import zachkingcade.dev.ledger.domain.exception.DomainException;

import static org.junit.jupiter.api.Assertions.*;

class AccountClassificationTest {

    @Test
    void rehydrate_creditEffectInvalid_throwsDomainException() {
        assertThrows(DomainException.class, () -> AccountClassification.rehydrate(1L, "desc", 'X', '+'));
    }

    @Test
    void rehydrate_debitEffectInvalid_throwsDomainException() {
        assertThrows(DomainException.class, () -> AccountClassification.rehydrate(1L, "desc", '+', 'X'));
    }

    @Test
    void rehydrate_validData_doesNotThrow() {
        AccountClassification classification = AccountClassification.rehydrate(1L, "desc", '+', '-');
        assertEquals(1L, classification.id());
        assertEquals("desc", classification.description());
        assertEquals('+', classification.creditEffect());
        assertEquals('-', classification.debitEffect());
    }
}

