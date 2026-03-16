package zachkingcade.dev.ledger.adapter.in.web.dto.account;

import java.util.Optional;

public record UpdateAccountRequest(
        Long id,
        Optional<String> description,
        Optional<String> notes,
        Optional<Boolean> active
) { }
