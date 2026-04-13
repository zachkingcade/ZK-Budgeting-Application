package zachkingcade.dev.reporting.application.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Formats ledger minor units (cents) for PDF reports: US currency, commas, two decimals, $ prefix.
 */
public final class ReportCurrencyFormat {

    private ReportCurrencyFormat() {
    }

    /**
     * @param minorUnits amount in cents (same as ledger / UI {@code dollarsStringToMinorUnits})
     */
    public static String formatMinorUnits(long minorUnits) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        BigDecimal dollars = BigDecimal.valueOf(minorUnits).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return nf.format(dollars);
    }
}
