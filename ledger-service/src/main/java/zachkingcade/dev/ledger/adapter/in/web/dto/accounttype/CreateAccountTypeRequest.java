package zachkingcade.dev.ledger.adapter.in.web.dto.accounttype;

import java.util.Optional;

public record CreateAccountTypeRequest(
        Long classificationId,
        String description,
        Optional<String> notes
) {
}
