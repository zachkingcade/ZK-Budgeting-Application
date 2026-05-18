package zachkingcade.dev.ledger.application.accountingperiod;

import java.time.LocalDate;

public final class AccountingPeriodViews {

    private AccountingPeriodViews() {
    }

    public record ClosedPeriodListItem(
            Long closedAccountingPeriodId,
            LocalDate startDate,
            LocalDate endDate,
            long transactionCount
    ) {
    }

    public record NextToCloseView(
            LocalDate startDate,
            LocalDate endDate,
            boolean closable,
            LocalDate closableAfter
    ) {
    }

    public record ArchivedJournalEntryView(
            Long journalEntryId,
            LocalDate entryDate,
            String description,
            String notes
    ) {
    }

    public record ClosedPeriodAccountBalanceView(
            Long accountId,
            long balance
    ) {
    }
}
