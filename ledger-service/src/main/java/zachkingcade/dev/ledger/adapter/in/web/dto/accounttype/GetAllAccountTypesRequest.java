package zachkingcade.dev.ledger.adapter.in.web.dto.accounttype;

import zachkingcade.dev.ledger.adapter.in.web.dto.shared.SortObject;
import zachkingcade.dev.ledger.application.validation.AccountTypeSortType;

import java.util.Optional;

public record GetAllAccountTypesRequest(
        Optional<SortObject<AccountTypeSortType>> sort
) {
}
