package zachkingcade.dev.ledger.domain.account;

import zachkingcade.dev.ledger.domain.journal.JournalLine;

import java.util.List;

public final class AccountBalanceCalculator {

    private AccountBalanceCalculator() {
    }

    public static long balanceFromLines(List<JournalLine> lines, AccountClassification classification) {
        long credit = 0L;
        long debit = 0L;
        for (JournalLine line : lines) {
            if (line.direction() == 'C') {
                credit += line.amount();
            } else {
                debit += line.amount();
            }
        }

        long resultingTotal = 0L;
        if (classification.creditEffect() == '+') {
            resultingTotal += credit;
        } else {
            resultingTotal -= credit;
        }
        if (classification.debitEffect() == '+') {
            resultingTotal += debit;
        } else {
            resultingTotal -= debit;
        }
        return resultingTotal;
    }
}
