package zachkingcade.dev.ledger.adapter.in.web.dto.accountclassifcation;

import java.util.List;

public record GetAllAccountClassificationResponse(
        List<AccountClassificationObject> accountClassificationList
) {
}
