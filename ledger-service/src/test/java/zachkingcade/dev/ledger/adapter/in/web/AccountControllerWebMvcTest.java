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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import zachkingcade.dev.ledger.adapter.in.web.config.LedgerJwtAuthorizationFilter;
import zachkingcade.dev.ledger.adapter.in.web.config.SecurityConfig;
import zachkingcade.dev.ledger.adapter.in.web.dto.GlobalExceptionHandler;
import zachkingcade.dev.ledger.application.exception.NotFoundException;
import zachkingcade.dev.ledger.application.port.in.account.CreateAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.account.GetAllAccountsUseCase;
import zachkingcade.dev.ledger.application.port.in.account.GetByIdAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.account.UpdateAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.accountclassification.GetAllAccountClassificationsUseCase;
import zachkingcade.dev.ledger.application.port.in.accountclassification.GetByIdAccountClassificationUseCase;
import zachkingcade.dev.ledger.application.port.in.accounttype.GetAllAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.in.accounttype.GetByIdAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.in.journal.GetBalanceForAccountUseCase;
import zachkingcade.dev.ledger.domain.account.Account;
import zachkingcade.dev.ledger.domain.account.AccountClassification;
import zachkingcade.dev.ledger.domain.account.AccountType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AccountController.class,
        excludeAutoConfiguration = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, FlywayAutoConfiguration.class}
)
@Import({SecurityConfig.class, LedgerJwtAuthorizationFilter.class, GlobalExceptionHandler.class})
class AccountControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private GetAllAccountsUseCase getAllAccountsUseCase;

    @MockitoBean
    private GetByIdAccountUseCase getByIdAccountUseCase;

    @MockitoBean
    private CreateAccountUseCase createAccountUseCase;

    @MockitoBean
    private UpdateAccountUseCase updateAccountUseCase;

    @MockitoBean
    private GetBalanceForAccountUseCase getBalanceForAccountUseCase;

    @MockitoBean
    private GetAllAccountTypeUseCase getAllAccountTypeUseCase;

    @MockitoBean
    private GetByIdAccountTypeUseCase getByIdAccountTypeUseCase;

    @MockitoBean
    private GetAllAccountClassificationsUseCase getAllAccountClassificationsUseCase;

    @MockitoBean
    private GetByIdAccountClassificationUseCase getByIdAccountClassificationUseCase;

    private static Jwt userJwt(long userId) {
        return Jwt.withTokenValue("t")
                .header("alg", "none")
                .subject(String.valueOf(userId))
                .claim("scope", "ledger.accounts.read ledger.journalentries.read")
                .build();
    }

    @Test
    void shouldCreateAccountWhenValidRequest() throws Exception {
        // HAPPY PATH
        when(createAccountUseCase.createAccount(any()))
                .thenReturn(Account.rehydrate(10L, 2L, "Checking", true, "", 1L));

        mvc.perform(post("/accounts/add")
                        .with(jwt().jwt(userJwt(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"typeId":2,"description":"Checking","notes":""}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accountId").value(10))
                .andExpect(jsonPath("$.data.typeId").value(2))
                .andExpect(jsonPath("$.data.description").value("Checking"));
    }

    @Test
    void shouldReturnAccountByIdWhenFound() throws Exception {
        // HAPPY PATH
        when(getByIdAccountUseCase.getAccountById(any()))
                .thenReturn(Account.rehydrate(10L, 2L, "Checking", true, "", 1L));
        when(getByIdAccountTypeUseCase.getAccountTypeById(any()))
                .thenReturn(AccountType.rehydrate(2L, "Cash", 1L, "", true, 1L, false));
        AccountClassification classification = AccountClassification.rehydrate(1L, "Asset", '+', '+');
        when(getByIdAccountClassificationUseCase.getByIdAccountClassification(1L))
                .thenReturn(classification);
        when(getBalanceForAccountUseCase.getBalanceForAccount(1L, 10L, classification))
                .thenReturn(5000L);

        mvc.perform(get("/accounts/byid/10")
                        .with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountId").value(10))
                .andExpect(jsonPath("$.data.accountBalance").value(5000));
    }

    @Test
    void shouldReturnNotFoundWhenAccountMissing() throws Exception {
        /*
        NEGATIVE PATH: method=GET /accounts/byid/{id},
        input={id: missing},
        expected failure message=Account not found
        */
        when(getByIdAccountUseCase.getAccountById(any()))
                .thenThrow(new NotFoundException("Account not found"));

        mvc.perform(get("/accounts/byid/999")
                        .with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Account not found"));
    }

    @Test
    void shouldAllowServiceTokenReadWhenScopePresent() throws Exception {
        // HAPPY PATH
        Jwt serviceJwt = Jwt.withTokenValue("svc")
                .header("alg", "none")
                .subject("reporting-service")
                .claim("token_type", "service")
                .claim("scope", "ledger.accounts.read")
                .claim("acting_for_user_id", 1)
                .build();

        when(getByIdAccountUseCase.getAccountById(any()))
                .thenReturn(Account.rehydrate(10L, 2L, "Checking", true, "", 1L));
        when(getByIdAccountTypeUseCase.getAccountTypeById(any()))
                .thenReturn(AccountType.rehydrate(2L, "Cash", 1L, "", true, 1L, false));
        AccountClassification classification = AccountClassification.rehydrate(1L, "Asset", '+', '+');
        when(getByIdAccountClassificationUseCase.getByIdAccountClassification(1L))
                .thenReturn(classification);
        when(getBalanceForAccountUseCase.getBalanceForAccount(1L, 10L, classification))
                .thenReturn(5000L);

        mvc.perform(get("/accounts/byid/10")
                        .with(jwt().jwt(serviceJwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountId").value(10))
                .andExpect(jsonPath("$.data.accountBalance").value(5000));
    }

    @Test
    void shouldForbidServiceTokenOnMutatingEndpoint() throws Exception {
        /*
        NEGATIVE PATH: method=POST /accounts/add,
        input=service token,
        expected failure message=Service tokens cannot access mutating endpoints
        */
        Jwt serviceJwt = Jwt.withTokenValue("svc")
                .header("alg", "none")
                .subject("reporting-service")
                .claim("token_type", "service")
                .claim("scope", "ledger.accounts.read")
                .build();

        mvc.perform(post("/accounts/add")
                        .with(jwt().jwt(serviceJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"typeId":2,"description":"Checking","notes":""}
                                """))
                .andExpect(status().isForbidden());
    }
}

