package zachkingcade.dev.ledger.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import zachkingcade.dev.ledger.adapter.in.web.config.LedgerJwtAuthorizationFilter;
import zachkingcade.dev.ledger.adapter.in.web.config.SecurityConfig;
import zachkingcade.dev.ledger.adapter.in.web.dto.GlobalExceptionHandler;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.port.in.replayablejournal.ReplayableJournalEntryUseCase;
import zachkingcade.dev.ledger.application.replayablejournal.ReplayableJournalEntryViews.ReplayableJournalEntryDetailView;
import zachkingcade.dev.ledger.application.replayablejournal.ReplayableJournalEntryViews.ReplayableJournalEntryListItem;
import zachkingcade.dev.ledger.application.replayablejournal.ReplayableJournalEntryViews.ReplayableJournalEntryLineView;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ReplayableJournalEntryController.class,
        excludeAutoConfiguration = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, FlywayAutoConfiguration.class})
@Import({SecurityConfig.class, LedgerJwtAuthorizationFilter.class, GlobalExceptionHandler.class})
class ReplayableJournalEntryControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private ReplayableJournalEntryUseCase replayableJournalEntryUseCase;

    private static Jwt userJwt(long userId) {
        return Jwt.withTokenValue("t")
                .header("alg", "none")
                .subject(String.valueOf(userId))
                .claim("scope", "ledger.read")
                .build();
    }

    @Test
    void shouldListReplayableJournalEntries() throws Exception {
        Instant t = Instant.parse("2026-01-01T12:00:00Z");
        when(replayableJournalEntryUseCase.list(eq(1L)))
                .thenReturn(List.of(new ReplayableJournalEntryListItem(3L, "Payroll", 2, t, t)));

        mvc.perform(get("/replayable-journal-entries").with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].replayName").value("Payroll"));
    }

    @Test
    void shouldCreateReplayableJournalEntry() throws Exception {
        Instant t = Instant.parse("2026-01-01T12:00:00Z");
        when(replayableJournalEntryUseCase.create(eq(1L), eq("Payroll"), org.mockito.ArgumentMatchers.anyList(), eq(true)))
                .thenReturn(new ReplayableJournalEntryDetailView(
                        3L, "Payroll", 2, t, t, List.of(new ReplayableJournalEntryLineView(100L, 1L, 'D'))));

        mvc.perform(post("/replayable-journal-entries")
                        .with(jwt().jwt(userJwt(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "replayName": "Payroll",
                                  "lines": [
                                    {"amount": 100, "accountId": 1, "direction": "D"},
                                    {"amount": 100, "accountId": 2, "direction": "C"}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.replayName").value("Payroll"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateRejected() throws Exception {
        when(replayableJournalEntryUseCase.create(eq(1L), eq("Payroll"), org.mockito.ArgumentMatchers.anyList(), eq(true)))
                .thenThrow(new ApplicationException("Replayable journal entry requires credit and debit totals to match."));

        mvc.perform(post("/replayable-journal-entries")
                        .with(jwt().jwt(userJwt(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "replayName": "Payroll",
                                  "lines": [
                                    {"amount": 100, "accountId": 1, "direction": "D"},
                                    {"amount": 50, "accountId": 2, "direction": "C"}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Replayable journal entry requires credit and debit totals to match."));
    }
}
