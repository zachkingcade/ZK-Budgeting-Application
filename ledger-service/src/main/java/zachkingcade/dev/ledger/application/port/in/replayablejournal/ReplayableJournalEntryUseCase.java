package zachkingcade.dev.ledger.application.port.in.replayablejournal;

import zachkingcade.dev.ledger.application.replayablejournal.ReplayableJournalEntryViews.ReplayableJournalEntryDetailView;
import zachkingcade.dev.ledger.application.replayablejournal.ReplayableJournalEntryViews.ReplayableJournalEntryListItem;
import zachkingcade.dev.ledger.application.replayablejournal.ReplayableJournalEntryViews.ReplayableJournalEntryLineView;

import java.util.List;

public interface ReplayableJournalEntryUseCase {

    List<ReplayableJournalEntryListItem> list(Long userId);

    ReplayableJournalEntryDetailView getById(Long userId, Long id);

    ReplayableJournalEntryDetailView create(
            Long userId, String replayName, List<ReplayableJournalEntryLineView> lines, boolean requireBalanced);

    ReplayableJournalEntryDetailView update(
            Long userId, Long id, String replayName, List<ReplayableJournalEntryLineView> lines, boolean requireBalanced);

    void delete(Long userId, Long id);
}
