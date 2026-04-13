package zachkingcade.dev.reporting.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import zachkingcade.dev.reporting.adapter.persistence.jpa.CompletedReportEntity;
import zachkingcade.dev.reporting.adapter.persistence.jpa.ReportJobEntity;
import zachkingcade.dev.reporting.adapter.persistence.repository.CompletedReportJpaRepository;
import zachkingcade.dev.reporting.adapter.persistence.repository.ReportJobJpaRepository;
import zachkingcade.dev.reporting.application.report.Report;
import zachkingcade.dev.reporting.application.report.ReportRegistry;
import zachkingcade.dev.reporting.domain.report.ReportJobStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ReportingApplicationService {

    private final ReportJobJpaRepository reportJobRepository;
    private final CompletedReportJpaRepository completedReportRepository;
    private final ReportRegistry reportRegistry;
    private final ObjectMapper objectMapper;

    public ReportingApplicationService(
            ReportJobJpaRepository reportJobRepository,
            CompletedReportJpaRepository completedReportRepository,
            ReportRegistry reportRegistry,
            ObjectMapper objectMapper
    ) {
        this.reportJobRepository = reportJobRepository;
        this.completedReportRepository = completedReportRepository;
        this.reportRegistry = reportRegistry;
        this.objectMapper = objectMapper;
    }

    public JsonNode catalog() {
        return reportRegistry.buildCatalog(objectMapper);
    }

    public ReportJobEntity requestReport(long owningUserId, String reportType, JsonNode parameters) {
        Report report = reportRegistry.get(reportType);
        if (report == null) {
            throw new IllegalArgumentException("Unknown report type: " + reportType);
        }
        report.validateParameters(parameters == null ? objectMapper.createObjectNode() : parameters);
        ReportJobEntity job = new ReportJobEntity();
        job.setOwningUserId(owningUserId);
        job.setReportType(reportType.trim().toUpperCase());
        job.setStatus(ReportJobStatus.QUEUED);
        try {
            job.setRequestParameters(objectMapper.writeValueAsString(parameters == null ? objectMapper.createObjectNode() : parameters));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid parameters");
        }
        job.setRequestedAt(Instant.now());
        return reportJobRepository.save(job);
    }

    public List<ReportJobEntity> listJobsForUser(long userId) {
        return reportJobRepository.findByOwningUserIdOrderByRequestedAtDesc(userId);
    }

    public Optional<ReportJobEntity> getJobForUser(long jobId, long userId) {
        return reportJobRepository.findByIdAndOwningUserId(jobId, userId);
    }

    public Optional<CompletedReportEntity> getCompletedPdf(long jobId, long userId) {
        Optional<ReportJobEntity> job = reportJobRepository.findByIdAndOwningUserId(jobId, userId);
        if (job.isEmpty() || job.get().getStatus() != ReportJobStatus.COMPLETED) {
            return Optional.empty();
        }
        return completedReportRepository.findByReportJobId(jobId);
    }
}
