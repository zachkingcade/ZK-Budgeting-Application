package zachkingcade.dev.ledger.application.commands.accounttype;

import java.util.Optional;

public record CreateAccountTypeCommand(
        Long classificationId,
        String description,
        Optional<String> notes
) { }
