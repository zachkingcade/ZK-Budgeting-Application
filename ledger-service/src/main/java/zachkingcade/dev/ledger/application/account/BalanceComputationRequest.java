package zachkingcade.dev.ledger.application.account;

import java.time.LocalDate;
import java.util.Optional;

public record BalanceComputationRequest(
        Optional<Long> baselineClosedPeriodId,
        Optional<LocalDate> liveEntryDateFrom,
        Optional<LocalDate> liveEntryDateTo
) {
    public static BalanceComputationRequest currentBalance(Optional<Long> latestClosedPeriodId) {
        return new BalanceComputationRequest(latestClosedPeriodId, Optional.empty(), Optional.empty());
    }

    public static BalanceComputationRequest forPeriodClose(
            Optional<Long> priorClosedPeriodId,
            LocalDate periodStart,
            LocalDate periodEnd) {
        return new BalanceComputationRequest(
                priorClosedPeriodId,
                Optional.of(periodStart),
                Optional.of(periodEnd));
    }
}
