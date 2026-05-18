package zachkingcade.dev.ledger.domain.accountingperiod;

import java.time.LocalDate;

public record PeriodWindow(LocalDate startDate, LocalDate endDate) {
}
