package zachkingcade.dev.ledger.domain.replayablejournal;

public record ReplayableJournalEntryLine(
        Long id,
        int lineOrder,
        long amount,
        long accountId,
        char direction) {

    public ReplayableJournalEntryLine {
        if (amount <= 0) {
            throw new IllegalArgumentException("Line amount must be positive");
        }
        if (direction != 'C' && direction != 'D') {
            throw new IllegalArgumentException("Line direction must be C or D");
        }
        if (accountId <= 0) {
            throw new IllegalArgumentException("Line requires a valid accountId");
        }
    }
}
