package zachkingcade.dev.ledger.domain.budgetplan;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BudgetPlanFrequencyMathTest {

    @Test
    void fourteenHundredEveryTwoWeeks_isOneHundredPerDay() {
        BigDecimal perDay = BudgetPlanFrequencyMath.amountPerDayMinorBd(140_000L, "weeks", 2);
        assertEquals(0, new BigDecimal("10000").compareTo(perDay));
    }

    @Test
    void sixtyEveryTwoMonths_isOneThousandPerDay() {
        BigDecimal perDay = BudgetPlanFrequencyMath.amountPerDayMinorBd(60_000L, "months", 2);
        assertEquals(0, new BigDecimal("1000").compareTo(perDay));
    }

    @Test
    void derivedPeriodsFromPerDay() {
        BigDecimal perDay = new BigDecimal("100");
        assertEquals(0, new BigDecimal("700").compareTo(BudgetPlanFrequencyMath.perWeekFromPerDay(perDay)));
        assertEquals(0, new BigDecimal("3000").compareTo(BudgetPlanFrequencyMath.perMonthFromPerDay(perDay)));
        assertEquals(0, new BigDecimal("36500").compareTo(BudgetPlanFrequencyMath.perYearFromPerDay(perDay)));
    }
}
