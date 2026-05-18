package zachkingcade.dev.ledger.domain.accountingperiod;

public enum AccountingPeriodFrequencyUnit {
    DAYS,
    WEEKS,
    MONTHS;

    public static AccountingPeriodFrequencyUnit fromSetting(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("frequency unit is required");
        }
        return AccountingPeriodFrequencyUnit.valueOf(value.trim().toUpperCase());
    }
}
