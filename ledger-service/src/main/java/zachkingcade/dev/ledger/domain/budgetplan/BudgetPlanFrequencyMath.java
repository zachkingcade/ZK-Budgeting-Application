package zachkingcade.dev.ledger.domain.budgetplan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Converts recurring amounts (minor units + frequency) to per-day minor units using a fixed 30/365 calendar:
 * 1 week = 7 days, 1 month = 30 days, 1 year = 365 days (not calendar months).
 * <p>Per-day values use high-precision {@link BigDecimal} internally; UI should round for display only.</p>
 */
public final class BudgetPlanFrequencyMath {

    private static final int DAYS_PER_WEEK = 7;
    private static final int DAYS_PER_MONTH = 30;
    private static final int DAYS_PER_YEAR = 365;

    /** Scale used for per-day division and derived period amounts (minor units). */
    public static final int MINOR_MATH_SCALE = 12;

    private BudgetPlanFrequencyMath() {
    }

    public static long periodDays(String frequencyUnit, int frequencyNumber) {
        Objects.requireNonNull(frequencyUnit, "frequencyUnit");
        if (frequencyNumber < 1) {
            throw new IllegalArgumentException("frequencyNumber must be >= 1");
        }
        return switch (frequencyUnit) {
            case "days" -> frequencyNumber;
            case "weeks" -> (long) frequencyNumber * DAYS_PER_WEEK;
            case "months" -> (long) frequencyNumber * DAYS_PER_MONTH;
            case "years" -> (long) frequencyNumber * DAYS_PER_YEAR;
            default -> throw new IllegalArgumentException("Invalid frequencyUnit: " + frequencyUnit);
        };
    }

    /**
     * Minor units per calendar day on the 30/365 basis (no premature rounding to whole cents).
     */
    public static BigDecimal amountPerDayMinorBd(long amountMinor, String frequencyUnit, int frequencyNumber) {
        long days = periodDays(frequencyUnit, frequencyNumber);
        if (days == 0) {
            throw new IllegalArgumentException("periodDays must be positive");
        }
        return BigDecimal.valueOf(amountMinor)
                .divide(BigDecimal.valueOf(days), MINOR_MATH_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal perWeekFromPerDay(BigDecimal amountPerDayMinor) {
        return amountPerDayMinor
                .multiply(BigDecimal.valueOf(DAYS_PER_WEEK))
                .setScale(MINOR_MATH_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal perMonthFromPerDay(BigDecimal amountPerDayMinor) {
        return amountPerDayMinor
                .multiply(BigDecimal.valueOf(DAYS_PER_MONTH))
                .setScale(MINOR_MATH_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal perYearFromPerDay(BigDecimal amountPerDayMinor) {
        return amountPerDayMinor
                .multiply(BigDecimal.valueOf(DAYS_PER_YEAR))
                .setScale(MINOR_MATH_SCALE, RoundingMode.HALF_UP);
    }
}
