package zachkingcade.dev.ledger.application.commands.account;

import java.util.Optional;

public record UpdateAccountCommand(
        Long id,
        Optional<String> description,
        Optional<String> notes,
        Optional<Boolean> active
) {
}
