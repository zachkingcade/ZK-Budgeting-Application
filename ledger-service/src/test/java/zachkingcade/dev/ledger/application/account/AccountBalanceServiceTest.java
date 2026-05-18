package zachkingcade.dev.ledger.application.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.ClosedAccountingPeriodEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.ClosedAccountingPeriodJpaRepository;
import zachkingcade.dev.ledger.application.port.out.accountingperiod.ClosedPeriodAccountBalanceRepositoryPort;
import zachkingcade.dev.ledger.application.port.out.journal.JournalEntryRepositoryPort;
import zachkingcade.dev.ledger.domain.account.AccountClassification;
import zachkingcade.dev.ledger.domain.journal.JournalLine;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountBalanceServiceTest {

    @Mock
    private JournalEntryRepositoryPort journalEntryRepository;

    @Mock
    private ClosedPeriodAccountBalanceRepositoryPort closedPeriodAccountBalanceRepository;

    @Mock
    private ClosedAccountingPeriodJpaRepository closedAccountingPeriodRepository;

    @InjectMocks
    private AccountBalanceService accountBalanceService;

    private final AccountClassification classification = AccountClassification.rehydrate(1L, "Asset", '-', '+');

    @Test
    void currentBalanceWithoutClosedPeriodUsesLiveOnly() {
        when(closedAccountingPeriodRepository.findTopByUserIdOrderByEndDateDesc(1L)).thenReturn(Optional.empty());
        when(journalEntryRepository.findLiveLinesByAccountId(eq(1L), eq(10L), eq(Optional.empty()), eq(Optional.empty())))
                .thenReturn(List.of(JournalLine.rehydrate(1L, 50L, 10L, 'D', "")));

        assertEquals(50L, accountBalanceService.currentBalance(1L, 10L, classification));
    }

    @Test
    void currentBalanceWithClosedPeriodUsesSnapshotPlusLive() {
        ClosedAccountingPeriodEntity period = new ClosedAccountingPeriodEntity();
        period.setClosedAccountingPeriodId(99L);
        when(closedAccountingPeriodRepository.findTopByUserIdOrderByEndDateDesc(1L)).thenReturn(Optional.of(period));
        when(closedPeriodAccountBalanceRepository.findBalancesByClosedPeriodIdAndUserId(99L, 1L))
                .thenReturn(Map.of(10L, 100L));
        when(journalEntryRepository.findLiveLinesByAccountId(eq(1L), eq(10L), eq(Optional.empty()), eq(Optional.empty())))
                .thenReturn(List.of(JournalLine.rehydrate(1L, 25L, 10L, 'D', "")));

        assertEquals(125L, accountBalanceService.currentBalance(1L, 10L, classification));
    }

    @Test
    void balanceThroughPeriodCloseUsesPriorSnapshotPlusPeriodLive() {
        when(closedPeriodAccountBalanceRepository.findBalancesByClosedPeriodIdAndUserId(5L, 1L))
                .thenReturn(Map.of(10L, 200L));
        when(journalEntryRepository.findLiveLinesByAccountId(
                eq(1L),
                eq(10L),
                eq(Optional.of(LocalDate.of(2026, 2, 1))),
                eq(Optional.of(LocalDate.of(2026, 2, 28)))))
                .thenReturn(List.of(JournalLine.rehydrate(1L, 30L, 10L, 'D', "")));

        long balance = accountBalanceService.balanceThroughPeriodClose(
                1L,
                10L,
                classification,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28),
                Optional.of(5L));

        assertEquals(230L, balance);
    }
}
