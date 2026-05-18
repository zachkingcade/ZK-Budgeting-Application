package zachkingcade.dev.ledger.application.port.in.accountingperiod;

import zachkingcade.dev.ledger.application.accountingperiod.AccountingPeriodViews.ArchivedJournalEntryView;
import zachkingcade.dev.ledger.application.accountingperiod.AccountingPeriodViews.ClosedPeriodAccountBalanceView;
import zachkingcade.dev.ledger.application.accountingperiod.AccountingPeriodViews.ClosedPeriodListItem;
import zachkingcade.dev.ledger.application.accountingperiod.AccountingPeriodViews.NextToCloseView;

import java.time.LocalDate;
import java.util.List;

public interface AccountingPeriodUseCase {

    List<ClosedPeriodListItem> listClosed(Long userId);

    List<ArchivedJournalEntryView> listArchivedJournalEntries(Long userId, Long closedPeriodId);

    List<ClosedPeriodAccountBalanceView> listAccountBalancesForClosedPeriod(Long userId, Long closedPeriodId);

    NextToCloseView getNextToClose(Long userId);

    ClosedPeriodListItem closePeriod(Long userId, LocalDate startDate, LocalDate endDate);
}
