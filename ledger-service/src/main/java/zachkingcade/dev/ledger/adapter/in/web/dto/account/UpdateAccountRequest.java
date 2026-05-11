package zachkingcade.dev.ledger.adapter.in.web.dto.account;

import java.util.Optional;

public record UpdateAccountRequest(
        Long id,
        Optional<String> description,
        /** Null if omitted (leave unchanged); empty string clears notes. */
        String notes,
        Optional<Boolean> active
    ) { }
