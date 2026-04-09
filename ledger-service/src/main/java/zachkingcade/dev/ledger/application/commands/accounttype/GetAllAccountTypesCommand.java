package zachkingcade.dev.ledger.application.commands.accounttype;

import zachkingcade.dev.ledger.application.commands.shared.SortObjectCommandObject;
import zachkingcade.dev.ledger.application.validation.AccountTypeSortType;

import java.util.Optional;

public record GetAllAccountTypesCommand(
        Long userId,
        Optional<SortObjectCommandObject<AccountTypeSortType>> sort,
        Optional<AccountTypeFilterCommandObject> filters
) {
}
