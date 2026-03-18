package zachkingcade.dev.ledger.adapter.in.web.dto.journal;

import java.util.List;

public record GetAllJournalEntryResponse(
        List<JournalEntryDTOResponse> journalEntryList
) {
}
