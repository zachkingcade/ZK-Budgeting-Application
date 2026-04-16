package zachkingcade.dev.ledger.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import zachkingcade.dev.ledger.adapter.in.web.config.LedgerJwtAuthorizationFilter;
import zachkingcade.dev.ledger.adapter.in.web.config.SecurityConfig;
import zachkingcade.dev.ledger.application.port.in.account.GetAllAccountsUseCase;
import zachkingcade.dev.ledger.application.port.in.account.GetByIdAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.accountclassification.GetAllAccountClassificationsUseCase;
import zachkingcade.dev.ledger.application.port.in.accountclassification.GetByIdAccountClassificationUseCase;
import zachkingcade.dev.ledger.application.port.in.accounttype.GetAllAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.in.accounttype.GetByIdAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.in.journal.CreateJournalEntryUseCase;
import zachkingcade.dev.ledger.application.port.in.journal.GetAllJournalEntryUseCase;
import zachkingcade.dev.ledger.application.port.in.journal.GetByIdJournalEntryUseCase;
import zachkingcade.dev.ledger.application.port.in.journal.RemoveByIdJournalEntryUseCase;
import zachkingcade.dev.ledger.application.port.in.journal.UpdateJournalEntryUseCase;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = JournalEntryController.class,
        excludeAutoConfiguration = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, FlywayAutoConfiguration.class}
)
@Import({SecurityConfig.class, LedgerJwtAuthorizationFilter.class})
class JournalEntryControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private GetAllJournalEntryUseCase getAllJournalEntryUseCase;

    @MockitoBean
    private GetByIdJournalEntryUseCase getByIdJournalEntryUseCase;

    @MockitoBean
    private CreateJournalEntryUseCase createJournalEntryUseCase;

    @MockitoBean
    private UpdateJournalEntryUseCase updateJournalEntryUseCase;

    @MockitoBean
    private RemoveByIdJournalEntryUseCase removeByIdJournalEntryUseCase;

    @MockitoBean
    private GetAllAccountClassificationsUseCase getAllAccountClassificationsUseCase;

    @MockitoBean
    private GetAllAccountTypeUseCase getAllAccountTypeUseCase;

    @MockitoBean
    private GetAllAccountsUseCase getAllAccountsUseCase;

    @MockitoBean
    private GetByIdAccountClassificationUseCase getByIdAccountClassificationUseCase;

    @MockitoBean
    private GetByIdAccountUseCase getByIdAccountUseCase;

    @MockitoBean
    private GetByIdAccountTypeUseCase getByIdAccountTypeUseCase;

    private static Jwt userJwt(long userId) {
        return Jwt.withTokenValue("t")
                .header("alg", "none")
                .subject(String.valueOf(userId))
                .claim("scope", "ledger.accounts.read ledger.journalentries.read")
                .build();
    }

    @Test
    void shouldReturnAllJournalEntriesWhenAuthenticated() throws Exception {
        // HAPPY PATH
        when(getAllJournalEntryUseCase.getAllJournalEntries(any())).thenReturn(List.of());
        when(getAllAccountsUseCase.getAllAccounts(any())).thenReturn(List.of());
        when(getAllAccountTypeUseCase.getAllAccountTypes(any())).thenReturn(List.of());
        when(getAllAccountClassificationsUseCase.getAllAccountClassifications()).thenReturn(List.of());

        mvc.perform(get("/journalentry/all")
                        .with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.journalEntryList").isArray());
    }

    @Test
    void shouldForbidServiceTokenWhenMissingJournalReadScope() throws Exception {
        /*
        NEGATIVE PATH: method=GET /journalentry/all,
        input=service token missing ledger.journalentries.read,
        expected failure message=Missing required scope: ledger.journalentries.read
        */
        Jwt serviceJwt = Jwt.withTokenValue("svc")
                .header("alg", "none")
                .subject("reporting-service")
                .claim("token_type", "service")
                .claim("scope", "ledger.accounts.read")
                .claim("acting_for_user_id", 1)
                .build();

        mvc.perform(get("/journalentry/all")
                        .with(jwt().jwt(serviceJwt)))
                .andExpect(status().isForbidden());
    }
}

