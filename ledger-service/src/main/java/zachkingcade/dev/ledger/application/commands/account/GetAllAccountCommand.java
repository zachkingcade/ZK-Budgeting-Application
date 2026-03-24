package zachkingcade.dev.ledger.application.commands.account;

import zachkingcade.dev.ledger.application.commands.shared.SortObjectCommandObject;
import zachkingcade.dev.ledger.application.validation.AccountSortType;

import java.util.Optional;

public record GetAllAccountCommand(
        Optional<SortObjectCommandObject<AccountSortType>> sort,
        Optional<AccountFilterCommandObject> filters
) {
}
