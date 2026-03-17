package zachkingcade.dev.ledger.adapter.in.web.dto.accounttype;

import java.util.List;

public record GetAllAccountTypesResponse(
        List<AccountTypeObject> accountTypeList
) {
}
