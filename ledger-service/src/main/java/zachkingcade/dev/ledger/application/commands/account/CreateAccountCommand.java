package zachkingcade.dev.ledger.application.commands.account;

import java.util.Optional;

public record CreateAccountCommand(
        Long userId,
        Long typeId,
        String description,
        Optional<String> notes
) { }
