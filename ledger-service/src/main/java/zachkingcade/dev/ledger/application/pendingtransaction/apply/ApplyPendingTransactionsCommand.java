package zachkingcade.dev.ledger.application.pendingtransaction.apply;

import zachkingcade.dev.ledger.adapter.in.web.dto.journal.JournalLineDTORequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record ApplyPendingTransactionsCommand(
        Long userId,
        List<Item> items
) {
    public record Item(
            Long pendingTransactionNumber,
            LocalDate entryDate,
            String description,
            Optional<String> notes,
            List<JournalLineDTORequest> journalLines
    ) {}
}

