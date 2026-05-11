package zachkingcade.dev.ledger.adapter.in.web.dto.account;

import java.util.List;

public record AccountsGroupedByTypeResponse(
        Long typeId,
        String typeName,
        List<AccountEnrichedObject> accounts
) {}
