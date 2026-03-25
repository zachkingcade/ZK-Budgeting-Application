package zachkingcade.dev.ledger.adapter.in.web.dto.account;

import zachkingcade.dev.ledger.domain.account.Account;

import java.util.List;

public record GetAllAccountsResponse(
        List<AccountEnrichedObject> accountsList
) { }
