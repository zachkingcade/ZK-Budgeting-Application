package zachkingcade.dev.ledger.adapter.in.web.dto.pendingtransaction;

import java.util.List;

public record GetAllPendingTransactionsResponse(
        List<PendingTransactionObject> pendingTransactions
) {}

