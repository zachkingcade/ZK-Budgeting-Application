package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.ClosedPeriodAccountBalanceEntity;

import java.util.List;

public interface ClosedPeriodAccountBalanceJpaRepository extends JpaRepository<ClosedPeriodAccountBalanceEntity, Long> {

    List<ClosedPeriodAccountBalanceEntity> findAllByClosedAccountingPeriodIdAndUserId(
            Long closedAccountingPeriodId,
            Long userId
    );
}
