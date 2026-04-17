package zachkingcade.dev.ledger.application.commands.accounttype;

import java.util.List;
import java.util.Optional;

public record AccountTypeFilterCommandObject(
        Optional<String> descriptionContains,
        Optional<String> notesContains,
        Optional<List<Long>> accountClass,
        Optional<Boolean> hideInactive,
        Optional<Boolean> hideActive,
        Optional<String> searchContains,
        Optional<Boolean> hideSystemAccounts
) {
}
