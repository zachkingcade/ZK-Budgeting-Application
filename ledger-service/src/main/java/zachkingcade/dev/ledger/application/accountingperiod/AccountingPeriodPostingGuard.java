package zachkingcade.dev.ledger.application.accountingperiod;

import org.springframework.stereotype.Component;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.ClosedAccountingPeriodJpaRepository;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.domain.accountingperiod.AccountingPeriodConfig;
import zachkingcade.dev.ledger.domain.accountingperiod.PeriodWindow;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

@Component
public class AccountingPeriodPostingGuard {

    private final ClosedAccountingPeriodJpaRepository closedPeriodRepository;
    private final AccountingPeriodSettingsResolver settingsResolver;
    private final AccountingPeriodBoundsService boundsService;

    public AccountingPeriodPostingGuard(
            ClosedAccountingPeriodJpaRepository closedPeriodRepository,
            AccountingPeriodSettingsResolver settingsResolver,
            AccountingPeriodBoundsService boundsService) {
        this.closedPeriodRepository = closedPeriodRepository;
        this.settingsResolver = settingsResolver;
        this.boundsService = boundsService;
    }

    public void assertEntryDateAllowed(Long userId, LocalDate entryDate) {
        if (entryDate == null) {
            throw new ApplicationException("entry date is required");
        }

        if (closedPeriodRepository.existsByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                userId, Date.valueOf(entryDate), Date.valueOf(entryDate))) {
            throw new ApplicationException("entry date falls in a closed accounting period");
        }

        Optional<AccountingPeriodConfig> config = settingsResolver.resolve(userId);
        if (config.isEmpty()) {
            return;
        }

        PeriodWindow oldestUnclosed = boundsService.oldestUnclosedPeriod(userId, config.get());
        if (entryDate.isBefore(oldestUnclosed.startDate())) {
            throw new ApplicationException(
                    "entry date is before the current open accounting period; post on or after "
                            + oldestUnclosed.startDate());
        }
    }
}
