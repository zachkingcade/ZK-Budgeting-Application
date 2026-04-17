package zachkingcade.dev.ledger.adapter.in.web.dto.account;

import java.util.List;
import java.util.Optional;

public record AccountFilterObject(
        Optional<String> descriptionContains,
        Optional<String> notesContains,
        Optional<List<Long>> accountTypes,
        Optional<Boolean> hideInactive,
        Optional<Boolean> hideActive,
        Optional<String> searchContains
) {
}
