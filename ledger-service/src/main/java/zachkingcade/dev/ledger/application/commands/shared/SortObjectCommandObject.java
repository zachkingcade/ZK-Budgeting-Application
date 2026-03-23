package zachkingcade.dev.ledger.application.commands.shared;

import zachkingcade.dev.ledger.application.validation.SortDirection;

import java.util.Optional;

public record SortObjectCommandObject <T extends Enum<T>>(
        T type,
        SortDirection direction
) {
}
