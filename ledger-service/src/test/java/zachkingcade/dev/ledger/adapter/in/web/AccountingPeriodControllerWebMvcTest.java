package zachkingcade.dev.ledger.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import zachkingcade.dev.ledger.adapter.in.web.config.JwtDecoderConfig;
import zachkingcade.dev.ledger.adapter.in.web.config.LedgerJwtAuthorizationFilter;
import zachkingcade.dev.ledger.adapter.in.web.config.SecurityConfig;
import zachkingcade.dev.ledger.adapter.in.web.dto.GlobalExceptionHandler;
import zachkingcade.dev.ledger.application.accountingperiod.AccountingPeriodViews.ClosedPeriodAccountBalanceView;
import zachkingcade.dev.ledger.application.accountingperiod.AccountingPeriodViews.ClosedPeriodListItem;
import zachkingcade.dev.ledger.application.accountingperiod.AccountingPeriodViews.NextToCloseView;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.port.in.accountingperiod.AccountingPeriodUseCase;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountingPeriodController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtDecoderConfig.class, LedgerJwtAuthorizationFilter.class})
class AccountingPeriodControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AccountingPeriodUseCase accountingPeriodUseCase;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldListClosedPeriods() throws Exception {
        when(accountingPeriodUseCase.listClosed(1L)).thenReturn(List.of(
                new ClosedPeriodListItem(5L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), 3L)));

        mvc.perform(get("/accounting-periods/closed").with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].transactionCount").value(3));
    }

    @Test
    void shouldReturnNextToClose() throws Exception {
        when(accountingPeriodUseCase.getNextToClose(1L))
                .thenReturn(new NextToCloseView(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), false, LocalDate.of(2026, 2, 28)));

        mvc.perform(get("/accounting-periods/next-to-close").with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.closable").value(false));
    }

    @Test
    void shouldClosePeriod() throws Exception {
        when(accountingPeriodUseCase.closePeriod(eq(1L), eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 1, 31))))
                .thenReturn(new ClosedPeriodListItem(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), 2L));

        mvc.perform(post("/accounting-periods/close")
                        .with(jwt().jwt(userJwt(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"startDate":"2026-01-01","endDate":"2026-01-31"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void shouldListAccountBalancesForClosedPeriod() throws Exception {
        when(accountingPeriodUseCase.listAccountBalancesForClosedPeriod(1L, 5L))
                .thenReturn(List.of(new ClosedPeriodAccountBalanceView(10L, 1500L)));

        mvc.perform(get("/accounting-periods/closed/5/account-balances").with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].accountId").value(10))
                .andExpect(jsonPath("$.data[0].balance").value(1500));
    }

    @Test
    void shouldReturnBadRequestWhenCloseRejected() throws Exception {
        when(accountingPeriodUseCase.closePeriod(eq(1L), eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 1, 31))))
                .thenThrow(new ApplicationException("Accounting period cannot be closed before it has ended"));

        mvc.perform(post("/accounting-periods/close")
                        .with(jwt().jwt(userJwt(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"startDate":"2026-01-01","endDate":"2026-01-31"}
                                """))
                .andExpect(status().isBadRequest());
    }

    private static Jwt userJwt(Long userId) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(String.valueOf(userId))
                .claim("token_type", "user")
                .claim("aud", List.of("ledger-service"))
                .issuer("auth-service")
                .build();
    }
}
