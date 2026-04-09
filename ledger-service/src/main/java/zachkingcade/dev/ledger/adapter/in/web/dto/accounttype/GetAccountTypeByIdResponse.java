package zachkingcade.dev.ledger.adapter.in.web.dto.accounttype;

import java.util.Optional;

public record GetAccountTypeByIdResponse(
        Long id,
        Long classificationId,
        String description,
        boolean active,
        String notes,
        boolean systemAccount
) {
}
