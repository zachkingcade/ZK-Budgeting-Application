package zachkingcade.dev.ledger.adapter.in.web.dto.account;

import java.util.List;
import java.util.Optional;

/**
 * @param accountsList Flat list: same order as returned from the service when not grouped; when grouped,
 *                     accounts in type order (per account type id ascending), then any remaining types.
 * @param groups       Present when grouped was requested; each subgroup uses the same sort as {@code accountsList} within that type.
 */
public record GetAllAccountsResponse(
        List<AccountEnrichedObject> accountsList,
        Optional<List<AccountsGroupedByTypeResponse>> groups
) { }
