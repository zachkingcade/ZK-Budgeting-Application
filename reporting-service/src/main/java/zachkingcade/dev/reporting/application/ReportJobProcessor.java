package zachkingcade.dev.reporting.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import zachkingcade.dev.reporting.adapter.outbound.LedgerApiClient;
import zachkingcade.dev.reporting.adapter.outbound.UserServiceTokenProvider;
import zachkingcade.dev.reporting.adapter.persistence.jpa.CompletedReportEntity;
import zachkingcade.dev.reporting.adapter.persistence.jpa.ReportJobEntity;
import zachkingcade.dev.reporting.adapter.persistence.repository.CompletedReportJpaRepository;
import zachkingcade.dev.reporting.application.report.Report;
import zachkingcade.dev.reporting.application.report.ReportContext;
import zachkingcade.dev.reporting.application.report.ReportRegistry;

import java.time.Instant;
import java.util.Optional;

@Service
public class ReportJobProcessor {

    private static final Logger log = LoggerFactory.getLogger(ReportJobProcessor.class);

    private final ReportJobLifecycleService reportJobLifecycleService;
    private final CompletedReportJpaRepository completedReportRepository;
    private final ReportRegistry reportRegistry;
    private final UserServiceTokenProvider userServiceTokenProvider;
    private final LedgerApiClient ledgerApiClient;
    private final ObjectMapper objectMapper;

    public ReportJobProcessor(
            ReportJobLifecycleService reportJobLifecycleService,
            CompletedReportJpaRepository completedReportRepository,
            ReportRegistry reportRegistry,
            UserServiceTokenProvider userServiceTokenProvider,
            LedgerApiClient ledgerApiClient,
            ObjectMapper objectMapper
    ) {
        this.reportJobLifecycleService = reportJobLifecycleService;
        this.completedReportRepository = completedReportRepository;
        this.reportRegistry = reportRegistry;
        this.userServiceTokenProvider = userServiceTokenProvider;
        this.ledgerApiClient = ledgerApiClient;
        this.objectMapper = objectMapper;
    }

    public synchronized void processOneQueuedJob() {
        Optional<ReportJobEntity> claimed = reportJobLifecycleService.claimNextQueued();
        if (claimed.isEmpty()) {
            return;
        }
        ReportJobEntity job = claimed.get();
        try {
            Report report = reportRegistry.get(job.getReportType());
            if (report == null) {
                throw new IllegalArgumentException("Unknown report type: " + job.getReportType());
            }
            JsonNode params = objectMapper.readTree(job.getRequestParameters());
            report.validateParameters(params);
            ReportContext ctx = new ReportContext(
                    job.getOwningUserId(),
                    params,
                    userServiceTokenProvider,
                    ledgerApiClient
            );
            byte[] pdf = report.generatePdf(ctx);
            CompletedReportEntity done = new CompletedReportEntity();
            done.setReportJobId(job.getId());
            done.setPdfContent(pdf);
            done.setContentType("application/pdf");
            done.setStoredAt(Instant.now());
            completedReportRepository.save(done);
            reportJobLifecycleService.markCompleted(job.getId());
        } catch (Exception e) {
            log.error("Report job failed id=[{}]", job.getId(), e);
            reportJobLifecycleService.markFailed(job.getId(), truncate(e.getMessage()));
        }
    }

    private static String truncate(String m) {
        if (m == null) {
            return "Unknown error";
        }
        return m.length() > 2000 ? m.substring(0, 2000) : m;
    }
}
