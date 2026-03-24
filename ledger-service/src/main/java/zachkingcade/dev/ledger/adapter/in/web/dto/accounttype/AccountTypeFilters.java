package zachkingcade.dev.ledger.adapter.in.web.dto.accounttype;

import java.util.List;
import java.util.Optional;

public record AccountTypeFilters(
        Optional<String> descriptionContains,
        Optional<String> notesContains,
        Optional<List<Long>> accountClass,
        Optional<Boolean> hideInactive,
        Optional<Boolean> hideActive
) {
}
