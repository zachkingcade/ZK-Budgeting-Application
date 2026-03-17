package zachkingcade.dev.ledger.adapter.in.web.dto.accounttype;

import java.util.Optional;

public record UpdateAccountTypeRequest(
        Long id,
        Optional<String> description,
        Optional<Boolean> active,
        Optional<String> notes
) {
}
