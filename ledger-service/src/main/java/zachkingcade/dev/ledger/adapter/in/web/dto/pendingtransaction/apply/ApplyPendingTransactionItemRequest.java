package zachkingcade.dev.ledger.adapter.in.web.dto.pendingtransaction.apply;

import zachkingcade.dev.ledger.adapter.in.web.dto.journal.JournalLineDTORequest;

import java.util.List;
import java.util.Optional;

public record ApplyPendingTransactionItemRequest(
        Long pendingTransactionNumber,
        String entryDate,
        String description,
        Optional<String> notes,
        List<JournalLineDTORequest> journalLines
) {}

