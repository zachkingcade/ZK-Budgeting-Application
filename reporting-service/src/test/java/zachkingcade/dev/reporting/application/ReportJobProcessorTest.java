package zachkingcade.dev.reporting.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import zachkingcade.dev.reporting.adapter.outbound.LedgerApiClient;
import zachkingcade.dev.reporting.adapter.outbound.UserServiceTokenProvider;
import zachkingcade.dev.reporting.adapter.persistence.jpa.ReportJobEntity;
import zachkingcade.dev.reporting.adapter.persistence.repository.CompletedReportJpaRepository;
import zachkingcade.dev.reporting.application.report.Report;
import zachkingcade.dev.reporting.application.report.ReportRegistry;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReportJobProcessorTest {

    @Test
    void shouldProcessQueuedJobWhenReportGeneratesPdf() throws Exception {
        // HAPPY PATH
        ReportJobLifecycleService lifecycle = mock(ReportJobLifecycleService.class);
        CompletedReportJpaRepository completedRepo = mock(CompletedReportJpaRepository.class);
        ReportRegistry registry = mock(ReportRegistry.class);
        UserServiceTokenProvider tokenProvider = mock(UserServiceTokenProvider.class);
        LedgerApiClient ledgerApiClient = mock(LedgerApiClient.class);
        ObjectMapper objectMapper = new ObjectMapper();

        ReportJobEntity job = new ReportJobEntity();
        job.setId(42L);
        job.setOwningUserId(1L);
        job.setReportType("ACCOUNT_BALANCE");
        job.setRequestParameters("{\"asOfDate\":\"2026-01-01\"}");

        when(lifecycle.claimNextQueued()).thenReturn(Optional.of(job));

        Report report = mock(Report.class);
        when(registry.get("ACCOUNT_BALANCE")).thenReturn(report);
        doNothing().when(report).validateParameters(any());
        when(report.generatePdf(any())).thenReturn(new byte[]{1, 2, 3});

        ReportJobProcessor processor = new ReportJobProcessor(
                lifecycle,
                completedRepo,
                registry,
                tokenProvider,
                ledgerApiClient,
                objectMapper
        );

        processor.processOneQueuedJob();

        verify(completedRepo, times(1)).save(any());
        verify(lifecycle, times(1)).markCompleted(42L);
        verify(lifecycle, never()).markFailed(eq(42L), any());
    }

    @Test
    void shouldDoNothingWhenNoQueuedJobs() {
        /*
        NEGATIVE PATH: method=processOneQueuedJob,
        input=no queued jobs,
        expected failure message=none (no-op)
        */
        ReportJobLifecycleService lifecycle = mock(ReportJobLifecycleService.class);
        CompletedReportJpaRepository completedRepo = mock(CompletedReportJpaRepository.class);
        ReportRegistry registry = mock(ReportRegistry.class);
        UserServiceTokenProvider tokenProvider = mock(UserServiceTokenProvider.class);
        LedgerApiClient ledgerApiClient = mock(LedgerApiClient.class);
        ObjectMapper objectMapper = new ObjectMapper();

        when(lifecycle.claimNextQueued()).thenReturn(Optional.empty());

        ReportJobProcessor processor = new ReportJobProcessor(
                lifecycle,
                completedRepo,
                registry,
                tokenProvider,
                ledgerApiClient,
                objectMapper
        );

        processor.processOneQueuedJob();

        verifyNoInteractions(completedRepo);
        verify(lifecycle, never()).markCompleted(anyLong());
        verify(lifecycle, never()).markFailed(anyLong(), any());
    }

    @Test
    void shouldMarkFailedWhenParametersInvalidJson() {
        /*
        NEGATIVE PATH: method=processOneQueuedJob,
        input=requestParameters=invalid JSON,
        expected failure message=Unexpected character
        */
        ReportJobLifecycleService lifecycle = mock(ReportJobLifecycleService.class);
        CompletedReportJpaRepository completedRepo = mock(CompletedReportJpaRepository.class);
        ReportRegistry registry = mock(ReportRegistry.class);
        UserServiceTokenProvider tokenProvider = mock(UserServiceTokenProvider.class);
        LedgerApiClient ledgerApiClient = mock(LedgerApiClient.class);
        ObjectMapper objectMapper = new ObjectMapper();

        ReportJobEntity job = new ReportJobEntity();
        job.setId(42L);
        job.setOwningUserId(1L);
        job.setReportType("ACCOUNT_BALANCE");
        job.setRequestParameters("{not-json");

        when(lifecycle.claimNextQueued()).thenReturn(Optional.of(job));
        when(registry.get("ACCOUNT_BALANCE")).thenReturn(mock(Report.class));

        ReportJobProcessor processor = new ReportJobProcessor(
                lifecycle,
                completedRepo,
                registry,
                tokenProvider,
                ledgerApiClient,
                objectMapper
        );

        processor.processOneQueuedJob();

        ArgumentCaptor<String> reason = ArgumentCaptor.forClass(String.class);
        verify(lifecycle).markFailed(eq(42L), reason.capture());
        verify(lifecycle, never()).markCompleted(anyLong());
        verify(completedRepo, never()).save(any());
        // message is Jackson-dependent; assert high-signal substring
        org.junit.jupiter.api.Assertions.assertTrue(
                reason.getValue() != null && reason.getValue().toLowerCase().contains("unexpected"),
                "Expected failure reason to mention an unexpected token/character"
        );
    }
}

