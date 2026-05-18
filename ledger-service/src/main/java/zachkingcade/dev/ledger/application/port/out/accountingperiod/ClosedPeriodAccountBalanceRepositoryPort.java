package zachkingcade.dev.ledger.application.port.out.accountingperiod;

import zachkingcade.dev.ledger.domain.accountingperiod.ClosedPeriodAccountBalance;

import java.util.List;
import java.util.Map;

public interface ClosedPeriodAccountBalanceRepositoryPort {

    Map<Long, Long> findBalancesByClosedPeriodIdAndUserId(Long closedAccountingPeriodId, Long userId);

    void saveAll(List<ClosedPeriodAccountBalance> balances);
}
