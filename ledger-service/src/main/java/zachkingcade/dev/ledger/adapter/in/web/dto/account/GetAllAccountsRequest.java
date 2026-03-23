package zachkingcade.dev.ledger.adapter.in.web.dto.account;

import zachkingcade.dev.ledger.adapter.in.web.dto.shared.SortObject;
import zachkingcade.dev.ledger.application.validation.AccountSortType;

import java.util.Optional;

public record GetAllAccountsRequest(
        Optional<SortObject<AccountSortType>> sort
) {
}
