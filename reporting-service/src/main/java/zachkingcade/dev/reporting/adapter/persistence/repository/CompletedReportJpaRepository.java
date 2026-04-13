package zachkingcade.dev.reporting.adapter.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.reporting.adapter.persistence.jpa.CompletedReportEntity;

import java.util.Optional;

public interface CompletedReportJpaRepository extends JpaRepository<CompletedReportEntity, Long> {

    Optional<CompletedReportEntity> findByReportJobId(Long reportJobId);
}
