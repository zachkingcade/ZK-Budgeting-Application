package zachkingcade.dev.ledger.domain.accountingperiod;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountingPeriodCalculatorTest {

    @Test
    void shouldComputeCalendarMonthPeriod() {
        AccountingPeriodConfig config = new AccountingPeriodConfig(
                LocalDate.of(2026, 1, 15),
                AccountingPeriodFrequencyUnit.MONTHS,
                1);

        PeriodWindow window = AccountingPeriodCalculator.periodWindowForStart(config, config.firstStartDate());

        assertEquals(LocalDate.of(2026, 1, 15), window.startDate());
        assertEquals(LocalDate.of(2026, 2, 14), window.endDate());
    }

    @Test
    void shouldComputeNextPeriodAfterClose() {
        AccountingPeriodConfig config = new AccountingPeriodConfig(
                LocalDate.of(2026, 1, 1),
                AccountingPeriodFrequencyUnit.DAYS,
                7);

        PeriodWindow first = AccountingPeriodCalculator.oldestUnclosedPeriod(config, Optional.empty());
        PeriodWindow second = AccountingPeriodCalculator.oldestUnclosedPeriod(config, Optional.of(first.endDate()));

        assertEquals(LocalDate.of(2026, 1, 8), second.startDate());
        assertEquals(LocalDate.of(2026, 1, 14), second.endDate());
    }
}
