package zachkingcade.dev.ledger.application.accountingperiod;

import org.springframework.stereotype.Component;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.ClosedAccountingPeriodEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.ClosedAccountingPeriodJpaRepository;
import zachkingcade.dev.ledger.domain.accountingperiod.AccountingPeriodCalculator;
import zachkingcade.dev.ledger.domain.accountingperiod.AccountingPeriodConfig;
import zachkingcade.dev.ledger.domain.accountingperiod.PeriodWindow;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class AccountingPeriodBoundsService {

    private final ClosedAccountingPeriodJpaRepository closedPeriodRepository;

    public AccountingPeriodBoundsService(ClosedAccountingPeriodJpaRepository closedPeriodRepository) {
        this.closedPeriodRepository = closedPeriodRepository;
    }

    public PeriodWindow oldestUnclosedPeriod(Long userId, AccountingPeriodConfig config) {
        Optional<LocalDate> lastClosedEnd = closedPeriodRepository.findTopByUserIdOrderByEndDateDesc(userId)
                .map(ClosedAccountingPeriodEntity::getEndDate)
                .map(java.sql.Date::toLocalDate);
        return AccountingPeriodCalculator.oldestUnclosedPeriod(config, lastClosedEnd);
    }
}
