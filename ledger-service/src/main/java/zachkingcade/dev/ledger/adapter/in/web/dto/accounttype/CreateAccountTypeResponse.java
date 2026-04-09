package zachkingcade.dev.ledger.adapter.in.web.dto.accounttype;

import java.util.Optional;

public record CreateAccountTypeResponse(
        Long id,
        Long classificationId,
        String description,
        boolean active,
        String notes,
        boolean systemAccount
) {
}
