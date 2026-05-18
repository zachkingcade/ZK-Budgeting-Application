package zachkingcade.dev.ledger.application.account;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.ClosedAccountingPeriodEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.ClosedAccountingPeriodJpaRepository;
import zachkingcade.dev.ledger.application.port.out.accountingperiod.ClosedPeriodAccountBalanceRepositoryPort;
import zachkingcade.dev.ledger.application.port.out.journal.JournalEntryRepositoryPort;
import zachkingcade.dev.ledger.domain.account.AccountBalanceCalculator;
import zachkingcade.dev.ledger.domain.account.AccountClassification;
import zachkingcade.dev.ledger.domain.journal.JournalLine;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountBalanceService {

    private final JournalEntryRepositoryPort journalEntryRepository;
    private final ClosedPeriodAccountBalanceRepositoryPort closedPeriodAccountBalanceRepository;
    private final ClosedAccountingPeriodJpaRepository closedAccountingPeriodRepository;

    public AccountBalanceService(
            JournalEntryRepositoryPort journalEntryRepository,
            ClosedPeriodAccountBalanceRepositoryPort closedPeriodAccountBalanceRepository,
            ClosedAccountingPeriodJpaRepository closedAccountingPeriodRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.closedPeriodAccountBalanceRepository = closedPeriodAccountBalanceRepository;
        this.closedAccountingPeriodRepository = closedAccountingPeriodRepository;
    }

    public long computeBalance(
            Long userId,
            Long accountId,
            AccountClassification classification,
            BalanceComputationRequest request) {
        long baseline = 0L;
        if (request.baselineClosedPeriodId().isPresent()) {
            Map<Long, Long> snapshots = closedPeriodAccountBalanceRepository.findBalancesByClosedPeriodIdAndUserId(
                    request.baselineClosedPeriodId().get(), userId);
            baseline = snapshots.getOrDefault(accountId, 0L);
        }

        List<JournalLine> lines = journalEntryRepository.findLiveLinesByAccountId(
                userId,
                accountId,
                request.liveEntryDateFrom(),
                request.liveEntryDateTo());

        return baseline + AccountBalanceCalculator.balanceFromLines(lines, classification);
    }

    public long currentBalance(Long userId, Long accountId, AccountClassification classification) {
        Optional<Long> latestPeriodId = closedAccountingPeriodRepository.findTopByUserIdOrderByEndDateDesc(userId)
                .map(ClosedAccountingPeriodEntity::getClosedAccountingPeriodId);
        return computeBalance(
                userId,
                accountId,
                classification,
                BalanceComputationRequest.currentBalance(latestPeriodId));
    }

    public long balanceThroughPeriodClose(
            Long userId,
            Long accountId,
            AccountClassification classification,
            LocalDate periodStart,
            LocalDate periodEnd,
            Optional<Long> priorClosedPeriodId) {
        return computeBalance(
                userId,
                accountId,
                classification,
                BalanceComputationRequest.forPeriodClose(priorClosedPeriodId, periodStart, periodEnd));
    }

    public Optional<Long> findLatestClosedPeriodId(Long userId) {
        return closedAccountingPeriodRepository.findTopByUserIdOrderByEndDateDesc(userId)
                .map(ClosedAccountingPeriodEntity::getClosedAccountingPeriodId);
    }

    public Optional<Long> findPriorClosedPeriodId(Long userId) {
        List<ClosedAccountingPeriodEntity> periods =
                closedAccountingPeriodRepository.findAllByUserIdOrderByEndDateDesc(userId);
        if (periods.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(periods.get(0).getClosedAccountingPeriodId());
    }
}
