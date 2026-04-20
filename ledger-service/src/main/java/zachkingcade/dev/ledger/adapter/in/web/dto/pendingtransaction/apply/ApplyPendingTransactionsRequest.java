package zachkingcade.dev.ledger.adapter.in.web.dto.pendingtransaction.apply;

import java.util.List;

public record ApplyPendingTransactionsRequest(
        List<ApplyPendingTransactionItemRequest> items
) {}

