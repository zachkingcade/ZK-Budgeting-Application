package zachkingcade.dev.ledger.application.replayablejournal;

import java.time.Instant;
import java.util.List;

public final class ReplayableJournalEntryViews {

    private ReplayableJournalEntryViews() {}

    public record ReplayableJournalEntryListItem(
            Long replayableJournalEntryId,
            String replayName,
            int replayLineCount,
            Instant createdAt,
            Instant lastEditedAt) {}

    public record ReplayableJournalEntryLineView(long amount, long accountId, char direction) {}

    public record ReplayableJournalEntryDetailView(
            Long replayableJournalEntryId,
            String replayName,
            int replayLineCount,
            Instant createdAt,
            Instant lastEditedAt,
            List<ReplayableJournalEntryLineView> lines) {}
}
