package zachkingcade.dev.ledger.application.commands.account;

import java.util.List;
import java.util.Optional;

public record AccountFilterCommandObject(
        Optional<String> descriptionContains,
        Optional<String> notesContains,
        Optional<List<Long>> accountTypes,
        Optional<Boolean> hideInactive,
        Optional<Boolean> hideActive,
        Optional<String> searchContains
) {
}
