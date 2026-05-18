package zachkingcade.dev.ledger.adapter.in.web.dto.accountingperiod;

import java.time.LocalDate;

public record CloseAccountingPeriodRequest(LocalDate startDate, LocalDate endDate) {
}
