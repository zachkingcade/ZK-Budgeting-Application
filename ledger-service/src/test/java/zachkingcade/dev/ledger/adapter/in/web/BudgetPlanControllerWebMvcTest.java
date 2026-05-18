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
import zachkingcade.dev.ledger.application.budgetplan.BudgetPlanningViews.BpBudgetResponse;
import zachkingcade.dev.ledger.application.budgetplan.BudgetPlanningViews.BudgetPlanListItem;
import zachkingcade.dev.ledger.application.port.in.budgetplan.BudgetPlanningUseCase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = BudgetPlanController.class,
        excludeAutoConfiguration = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, FlywayAutoConfiguration.class})
@Import({SecurityConfig.class, LedgerJwtAuthorizationFilter.class, GlobalExceptionHandler.class})
class BudgetPlanControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private BudgetPlanningUseCase budgetPlanningUseCase;

    private static Jwt userJwt(long userId) {
        return Jwt.withTokenValue("t")
                .header("alg", "none")
                .subject(String.valueOf(userId))
                .claim("scope", "ledger.read")
                .build();
    }

    @Test
    void shouldListBudgetPlans() throws Exception {
        Instant t = Instant.parse("2026-01-01T12:00:00Z");
        when(budgetPlanningUseCase.listPlans(eq(1L)))
                .thenReturn(List.of(new BudgetPlanListItem(9L, "Plan A", t, t)));

        mvc.perform(get("/budget-plans").with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].budgetPlanId").value(9))
                .andExpect(jsonPath("$.data[0].name").value("Plan A"));
    }

    @Test
    void shouldDuplicateBudgetPlan() throws Exception {
        Instant t = Instant.parse("2026-03-01T12:00:00Z");
        when(budgetPlanningUseCase.duplicatePlan(eq(1L), eq(9L), eq("Copy"), eq(true), eq(true), eq(false)))
                .thenReturn(new BudgetPlanListItem(12L, "Copy", t, t));

        mvc.perform(post("/budget-plans/9/duplicate")
                        .with(jwt().jwt(userJwt(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"name\":\"Copy\",\"copyIncomes\":true,\"copyRecurringPayables\":true,\"copyBudgetAmounts\":false}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.budgetPlanId").value(12))
                .andExpect(jsonPath("$.data.name").value("Copy"));
    }

    @Test
    void shouldCreateBudgetPlan() throws Exception {
        Instant t = Instant.parse("2026-01-02T12:00:00Z");
        when(budgetPlanningUseCase.createPlan(eq(1L), eq("New")))
                .thenReturn(new BudgetPlanListItem(3L, "New", t, t));

        mvc.perform(post("/budget-plans")
                        .with(jwt().jwt(userJwt(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.budgetPlanId").value(3))
                .andExpect(jsonPath("$.data.name").value("New"));
    }

    @Test
    void shouldUpdateBudgetWithPutBody() throws Exception {
        BpBudgetResponse updated =
                new BpBudgetResponse(
                        1L,
                        50L,
                        new BigDecimal("500"),
                        new BigDecimal("3500"),
                        new BigDecimal("15000"),
                        new BigDecimal("182500"),
                        14L,
                        "Groceries");
        when(budgetPlanningUseCase.updateBudget(
                        eq(1L), eq(50L), eq(1L), eq(50000L), eq("months"), eq(1), eq(14L)))
                .thenReturn(updated);

        mvc.perform(put("/budget-plans/50/budgets/1")
                        .with(jwt().jwt(userJwt(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountMinor\":50000,\"frequencyUnit\":\"months\",\"frequencyNumber\":1,\"accountId\":14}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bpBudgetId").value(1))
                .andExpect(jsonPath("$.data.accountId").value(14));

        mvc.perform(patch("/budget-plans/50/budgets/1")
                        .with(jwt().jwt(userJwt(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountMinor\":50000,\"frequencyUnit\":\"months\",\"frequencyNumber\":1,\"accountId\":14}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bpBudgetId").value(1));
    }
}
