package zachkingcade.dev.reporting.adapter.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.reporting.adapter.persistence.jpa.ReportJobEntity;
import zachkingcade.dev.reporting.domain.report.ReportJobStatus;

import java.util.List;
import java.util.Optional;

public interface ReportJobJpaRepository extends JpaRepository<ReportJobEntity, Long> {

    List<ReportJobEntity> findByOwningUserIdOrderByRequestedAtDesc(Long owningUserId);

    Optional<ReportJobEntity> findByIdAndOwningUserId(Long id, Long owningUserId);

    Optional<ReportJobEntity> findFirstByStatusOrderByRequestedAtAsc(ReportJobStatus status);
}
