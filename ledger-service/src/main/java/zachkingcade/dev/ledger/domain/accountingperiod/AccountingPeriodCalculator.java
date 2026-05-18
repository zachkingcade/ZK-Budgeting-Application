package zachkingcade.dev.ledger.domain.accountingperiod;

import java.time.LocalDate;
import java.util.Optional;

public final class AccountingPeriodCalculator {

    private AccountingPeriodCalculator() {
    }

    public static PeriodWindow periodWindowForStart(AccountingPeriodConfig config, LocalDate periodStart) {
        LocalDate end = computeEndDate(periodStart, config.unit(), config.count());
        return new PeriodWindow(periodStart, end);
    }

    public static LocalDate computeEndDate(LocalDate periodStart, AccountingPeriodFrequencyUnit unit, int count) {
        if (count < 1) {
            throw new IllegalArgumentException("frequency count must be at least 1");
        }
        return switch (unit) {
            case DAYS -> periodStart.plusDays(count - 1L);
            case WEEKS -> periodStart.plusWeeks(count).minusDays(1);
            case MONTHS -> periodStart.plusMonths(count).minusDays(1);
        };
    }

    public static LocalDate nextPeriodStart(LocalDate firstStartDate, Optional<LocalDate> lastClosedEndDate) {
        return lastClosedEndDate.map(end -> end.plusDays(1)).orElse(firstStartDate);
    }

    public static PeriodWindow oldestUnclosedPeriod(AccountingPeriodConfig config, Optional<LocalDate> lastClosedEndDate) {
        LocalDate start = nextPeriodStart(config.firstStartDate(), lastClosedEndDate);
        return periodWindowForStart(config, start);
    }
}
