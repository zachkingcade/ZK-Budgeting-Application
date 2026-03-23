package zachkingcade.dev.ledger.adapter.in.web.dto.journal;

import zachkingcade.dev.ledger.adapter.in.web.dto.shared.SortObject;
import zachkingcade.dev.ledger.application.validation.JournalEntrySortType;

import java.util.Optional;

public record GetAllJournalEntryRequest(
        Optional<SortObject<JournalEntrySortType>> sort
) {
}
