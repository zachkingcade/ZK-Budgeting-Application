package zachkingcade.dev.ledger.domain.replayablejournal;

import zachkingcade.dev.ledger.application.exception.ApplicationException;

import java.util.List;

public final class ReplayableJournalEntryValidator {

    private ReplayableJournalEntryValidator() {}

    public static void validateName(String replayName) {
        if (replayName == null || replayName.isBlank()) {
            throw new ApplicationException("Replay name is required.");
        }
    }

    public static void validateLines(List<ReplayableJournalEntryLine> lines, boolean requireBalanced) {
        if (lines == null || lines.isEmpty()) {
            throw new ApplicationException("Replayable journal entry requires at least one line.");
        }
        if (requireBalanced && lines.size() < 2) {
            throw new ApplicationException("Replayable journal entry requires at least two lines.");
        }
        if (!requireBalanced) {
            return;
        }
        long credit = 0L;
        long debit = 0L;
        for (ReplayableJournalEntryLine line : lines) {
            if (line.direction() == 'C') {
                credit += line.amount();
            } else {
                debit += line.amount();
            }
        }
        if (credit != debit) {
            throw new ApplicationException("Replayable journal entry requires credit and debit totals to match.");
        }
    }
}
