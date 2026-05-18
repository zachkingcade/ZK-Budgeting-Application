package zachkingcade.dev.ledger.domain.accountingperiod;

public record ClosedPeriodAccountBalance(
        Long closedPeriodAccountBalanceId,
        Long closedAccountingPeriodId,
        Long userId,
        Long accountId,
        long balance
) {
}
