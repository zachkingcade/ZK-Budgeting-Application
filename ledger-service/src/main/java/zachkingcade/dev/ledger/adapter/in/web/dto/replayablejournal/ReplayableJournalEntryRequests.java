package zachkingcade.dev.ledger.adapter.in.web.dto.replayablejournal;

import java.util.List;

public final class ReplayableJournalEntryRequests {

    private ReplayableJournalEntryRequests() {}

    public record ReplayableJournalEntryLineRequest(Long amount, Long accountId, Character direction) {}

    public record SaveReplayableJournalEntryRequest(
            String replayName, List<ReplayableJournalEntryLineRequest> lines, Boolean requireBalanced) {}
}
