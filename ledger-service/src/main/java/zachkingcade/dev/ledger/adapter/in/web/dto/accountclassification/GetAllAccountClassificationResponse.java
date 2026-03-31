package zachkingcade.dev.ledger.adapter.in.web.dto.accountclassification;

import java.util.List;

public record GetAllAccountClassificationResponse(
        List<AccountClassificationObject> accountClassificationList
) {
}
