package zachkingcade.dev.reporting.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import zachkingcade.dev.reporting.adapter.persistence.jpa.ReportJobEntity;
import zachkingcade.dev.reporting.adapter.persistence.repository.ReportJobJpaRepository;
import zachkingcade.dev.reporting.domain.report.ReportJobStatus;

import java.time.Instant;
import java.util.Optional;

@Service
public class ReportJobLifecycleService {

    private final ReportJobJpaRepository reportJobRepository;

    public ReportJobLifecycleService(ReportJobJpaRepository reportJobRepository) {
        this.reportJobRepository = reportJobRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<ReportJobEntity> claimNextQueued() {
        Optional<ReportJobEntity> pick = reportJobRepository.findFirstByStatusOrderByRequestedAtAsc(ReportJobStatus.QUEUED);
        if (pick.isEmpty()) {
            return Optional.empty();
        }
        ReportJobEntity j = pick.get();
        j.setStatus(ReportJobStatus.IN_PROGRESS);
        j.setStartedAt(Instant.now());
        return Optional.of(reportJobRepository.save(j));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(long jobId) {
        ReportJobEntity j = reportJobRepository.findById(jobId).orElseThrow();
        j.setStatus(ReportJobStatus.COMPLETED);
        j.setCompletedAt(Instant.now());
        reportJobRepository.save(j);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(long jobId, String reason) {
        ReportJobEntity j = reportJobRepository.findById(jobId).orElseThrow();
        j.setStatus(ReportJobStatus.FAILED);
        j.setFailureReason(reason);
        j.setCompletedAt(Instant.now());
        reportJobRepository.save(j);
    }
}
