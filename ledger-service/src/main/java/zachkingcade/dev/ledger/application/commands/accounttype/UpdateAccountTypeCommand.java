package zachkingcade.dev.ledger.application.commands.accounttype;

import java.util.Optional;

public record UpdateAccountTypeCommand(
        Long userId,
        Long id,
        Optional<String> description,
        Optional<String> notes,
        Optional<Boolean> active
) {
}
