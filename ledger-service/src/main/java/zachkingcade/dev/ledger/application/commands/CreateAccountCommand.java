package zachkingcade.dev.ledger.application.commands;

import java.util.Optional;

public record CreateAccountCommand(
        Long typeId,
        String description,
        Optional<String> notes
) { }
