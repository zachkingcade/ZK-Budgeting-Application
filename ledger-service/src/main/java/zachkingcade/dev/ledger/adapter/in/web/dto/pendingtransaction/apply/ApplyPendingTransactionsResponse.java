package zachkingcade.dev.ledger.adapter.in.web.dto.pendingtransaction.apply;

import java.util.List;

public record ApplyPendingTransactionsResponse(
        long successCount,
        long failureCount,
        List<ApplyPendingTransactionsSuccessObject> succeeded,
        List<ApplyPendingTransactionsFailureObject> failed
) {}

