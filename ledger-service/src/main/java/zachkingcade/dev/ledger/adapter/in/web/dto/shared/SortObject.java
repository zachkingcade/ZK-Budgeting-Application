package zachkingcade.dev.ledger.adapter.in.web.dto.shared;

import jakarta.annotation.Nullable;
import zachkingcade.dev.ledger.application.validation.SortDirection;

public record SortObject <T extends Enum<T>>(
        T type,

        @Nullable
        SortDirection direction
) {
}
