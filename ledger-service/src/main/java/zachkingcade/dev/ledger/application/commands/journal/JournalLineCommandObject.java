package zachkingcade.dev.ledger.application.commands.journal;

import java.util.Optional;

public record JournalLineCommandObject(
        Long amount,
        Long accountId,
        char direction,
        Optional<String> notes
) {
}
