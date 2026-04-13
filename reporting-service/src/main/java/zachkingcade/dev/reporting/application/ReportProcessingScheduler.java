package zachkingcade.dev.reporting.application;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReportProcessingScheduler {

    private final ReportJobProcessor reportJobProcessor;

    public ReportProcessingScheduler(ReportJobProcessor reportJobProcessor) {
        this.reportJobProcessor = reportJobProcessor;
    }

    @Scheduled(fixedDelay = 8000)
    public void poll() {
        reportJobProcessor.processOneQueuedJob();
    }
}
