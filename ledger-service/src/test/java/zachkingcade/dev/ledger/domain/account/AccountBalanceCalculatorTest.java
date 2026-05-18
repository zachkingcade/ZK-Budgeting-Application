package zachkingcade.dev.ledger.domain.account;

import org.junit.jupiter.api.Test;
import zachkingcade.dev.ledger.domain.journal.JournalLine;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountBalanceCalculatorTest {

    @Test
    void shouldApplyCreditAndDebitEffects() {
        AccountClassification asset = AccountClassification.rehydrate(1L, "Asset", '-', '+');
        List<JournalLine> lines = List.of(
                JournalLine.rehydrate(1L, 100L, 10L, 'D', ""),
                JournalLine.rehydrate(2L, 40L, 10L, 'C', ""));

        assertEquals(60L, AccountBalanceCalculator.balanceFromLines(lines, asset));
    }
}
