package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.ClosedAccountingPeriodEntity;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

public interface ClosedAccountingPeriodJpaRepository extends JpaRepository<ClosedAccountingPeriodEntity, Long> {

    List<ClosedAccountingPeriodEntity> findAllByUserIdOrderByEndDateDesc(Long userId);

    Optional<ClosedAccountingPeriodEntity> findByClosedAccountingPeriodIdAndUserId(Long id, Long userId);

    Optional<ClosedAccountingPeriodEntity> findTopByUserIdOrderByEndDateDesc(Long userId);

    boolean existsByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long userId, Date date, Date sameDate);
}
