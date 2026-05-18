package zachkingcade.dev.ledger.adapter.out.persistence;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.ClosedPeriodAccountBalanceEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.ClosedPeriodAccountBalanceJpaRepository;
import zachkingcade.dev.ledger.application.port.out.accountingperiod.ClosedPeriodAccountBalanceRepositoryPort;
import zachkingcade.dev.ledger.domain.accountingperiod.ClosedPeriodAccountBalance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClosedPeriodAccountBalancePersistenceAdapter implements ClosedPeriodAccountBalanceRepositoryPort {

    private final ClosedPeriodAccountBalanceJpaRepository repository;

    public ClosedPeriodAccountBalancePersistenceAdapter(ClosedPeriodAccountBalanceJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Map<Long, Long> findBalancesByClosedPeriodIdAndUserId(Long closedAccountingPeriodId, Long userId) {
        Map<Long, Long> byAccountId = new HashMap<>();
        for (ClosedPeriodAccountBalanceEntity entity :
                repository.findAllByClosedAccountingPeriodIdAndUserId(closedAccountingPeriodId, userId)) {
            byAccountId.put(entity.getAccountId(), entity.getBalance());
        }
        return byAccountId;
    }

    @Override
    public void saveAll(List<ClosedPeriodAccountBalance> balances) {
        List<ClosedPeriodAccountBalanceEntity> entities = balances.stream().map(this::toEntity).toList();
        repository.saveAll(entities);
    }

    private ClosedPeriodAccountBalanceEntity toEntity(ClosedPeriodAccountBalance balance) {
        ClosedPeriodAccountBalanceEntity entity = new ClosedPeriodAccountBalanceEntity();
        if (balance.closedPeriodAccountBalanceId() != null) {
            entity.setClosedPeriodAccountBalanceId(balance.closedPeriodAccountBalanceId());
        }
        entity.setClosedAccountingPeriodId(balance.closedAccountingPeriodId());
        entity.setUserId(balance.userId());
        entity.setAccountId(balance.accountId());
        entity.setBalance(balance.balance());
        return entity;
    }
}
