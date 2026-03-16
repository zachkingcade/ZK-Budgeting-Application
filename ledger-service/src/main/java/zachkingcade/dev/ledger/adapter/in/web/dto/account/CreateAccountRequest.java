package zachkingcade.dev.ledger.adapter.in.web.dto.account;

import java.util.Optional;

public record CreateAccountRequest(
        Long typeId,
        String description,
        Optional<String> notes
) {
}
