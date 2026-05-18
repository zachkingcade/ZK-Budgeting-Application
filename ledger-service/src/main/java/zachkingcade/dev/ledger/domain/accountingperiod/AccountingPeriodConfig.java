package zachkingcade.dev.ledger.domain.accountingperiod;

import java.time.LocalDate;

public record AccountingPeriodConfig(
        LocalDate firstStartDate,
        AccountingPeriodFrequencyUnit unit,
        int count
) {
}
