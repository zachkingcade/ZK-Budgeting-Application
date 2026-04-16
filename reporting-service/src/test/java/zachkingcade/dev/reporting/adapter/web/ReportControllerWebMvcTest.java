package zachkingcade.dev.reporting.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import zachkingcade.dev.reporting.adapter.persistence.jpa.CompletedReportEntity;
import zachkingcade.dev.reporting.adapter.persistence.jpa.ReportJobEntity;
import zachkingcade.dev.reporting.adapter.web.config.SecurityConfig;
import zachkingcade.dev.reporting.adapter.web.dto.GlobalExceptionHandler;
import zachkingcade.dev.reporting.application.ReportingApplicationService;
import zachkingcade.dev.reporting.domain.report.ReportJobStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, ReportControllerWebMvcTest.TestConfig.class})
class ReportControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private ReportingApplicationService reportingApplicationService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    private static Jwt jwtWithSubject(String subject) {
        return Jwt.withTokenValue("t")
                .header("alg", "none")
                .subject(subject)
                .claim("scope", "reports.read")
                .build();
    }

    @Test
    void shouldReturnCatalogWhenAuthenticated() throws Exception {
        // HAPPY PATH
        when(reportingApplicationService.catalog()).thenReturn(objectMapper.readTree("""
                [
                  {"code":"TRANSACTION_SUMMARY","displayName":"Transaction summary","description":"Transaction summary","fields":[]},
                  {"code":"ACCOUNT_BALANCE","displayName":"Account balance","description":"Account balance","fields":[]}
                ]
                """));

        mvc.perform(get("/reports/catalog")
                        .with(jwt().jwt(jwtWithSubject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].code").value("ACCOUNT_BALANCE"))
                .andExpect(jsonPath("$.data[1].code").value("TRANSACTION_SUMMARY"));
    }

    @Test
    void shouldReturnUnauthorizedWhenMissingJwt() throws Exception {
        /*
        NEGATIVE PATH: method=GET /reports/catalog,
        input=unauthenticated,
        expected failure message=401 Unauthorized
        */
        mvc.perform(get("/reports/catalog"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldQueueReportWhenValidRequest() throws Exception {
        // HAPPY PATH
        ReportJobEntity job = new ReportJobEntity();
        job.setId(99L);
        job.setOwningUserId(1L);
        job.setReportType("ACCOUNT_BALANCE");
        job.setStatus(ReportJobStatus.QUEUED);
        job.setRequestParameters("{\"asOfDate\":\"2026-01-01\"}");
        job.setRequestedAt(Instant.parse("2026-01-01T00:00:00Z"));

        when(reportingApplicationService.requestReport(anyLong(), any(), any())).thenReturn(job);

        mvc.perform(post("/reports/requests")
                        .with(jwt().jwt(jwtWithSubject("1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reportType":"ACCOUNT_BALANCE","parameters":{"asOfDate":"2026-01-01"}}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.id").value(99))
                .andExpect(jsonPath("$.data.reportType").value("ACCOUNT_BALANCE"))
                .andExpect(jsonPath("$.data.status").value("QUEUED"));
    }

    @Test
    void shouldReturnBadRequestWhenMissingReportType() throws Exception {
        /*
        NEGATIVE PATH: method=POST /reports/requests,
        input={reportType: blank},
        expected failure message=reportType is required
        */
        mvc.perform(post("/reports/requests")
                        .with(jwt().jwt(jwtWithSubject("1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reportType":" ","parameters":{}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("reportType is required"));
    }

    @Test
    void shouldListJobsForUser() throws Exception {
        // HAPPY PATH
        ReportJobEntity job = new ReportJobEntity();
        job.setId(1L);
        job.setOwningUserId(1L);
        job.setReportType("ACCOUNT_BALANCE");
        job.setStatus(ReportJobStatus.QUEUED);
        job.setRequestParameters("{}");
        job.setRequestedAt(Instant.parse("2026-01-01T00:00:00Z"));

        when(reportingApplicationService.listJobsForUser(1L)).thenReturn(List.of(job));

        mvc.perform(get("/reports")
                        .with(jwt().jwt(jwtWithSubject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void shouldReturnNotFoundWhenJobNotOwnedOrMissing() throws Exception {
        /*
        NEGATIVE PATH: method=GET /reports/{id},
        input={id: 123 not owned},
        expected failure message=Report not found
        */
        when(reportingApplicationService.getJobForUser(123L, 1L)).thenReturn(Optional.empty());

        mvc.perform(get("/reports/123")
                        .with(jwt().jwt(jwtWithSubject("1"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDownloadPdfWhenCompleted() throws Exception {
        // HAPPY PATH
        CompletedReportEntity done = new CompletedReportEntity();
        done.setReportJobId(10L);
        done.setPdfContent(new byte[]{0x25, 0x50, 0x44, 0x46}); // %PDF
        done.setContentType("application/pdf");

        when(reportingApplicationService.getCompletedPdf(10L, 1L)).thenReturn(Optional.of(done));

        mvc.perform(get("/reports/10/download")
                        .with(jwt().jwt(jwtWithSubject("1"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void shouldReturnNotFoundWhenPdfNotAvailable() throws Exception {
        /*
        NEGATIVE PATH: method=GET /reports/{id}/download,
        input={id: 10 not completed},
        expected failure message=Report is not available for download
        */
        when(reportingApplicationService.getCompletedPdf(10L, 1L)).thenReturn(Optional.empty());

        mvc.perform(get("/reports/10/download")
                        .with(jwt().jwt(jwtWithSubject("1"))))
                .andExpect(status().isNotFound());
    }
}

