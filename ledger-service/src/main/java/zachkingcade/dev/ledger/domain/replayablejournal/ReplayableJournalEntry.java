package zachkingcade.dev.ledger.domain.replayablejournal;

import java.time.Instant;
import java.util.List;

public record ReplayableJournalEntry(
        Long id,
        long userId,
        String replayName,
        int replayLineCount,
        Instant createdAt,
        Instant lastEditedAt,
        List<ReplayableJournalEntryLine> lines) {}
