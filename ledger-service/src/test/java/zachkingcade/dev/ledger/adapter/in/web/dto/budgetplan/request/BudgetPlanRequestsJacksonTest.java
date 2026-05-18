package zachkingcade.dev.ledger.adapter.in.web.dto.budgetplan.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BudgetPlanRequestsJacksonTest {

    @Test
    void updateBpBudgetRequest_deserializesFromClientJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        String json = "{\"amountMinor\":50000,\"frequencyUnit\":\"months\",\"frequencyNumber\":1,\"accountId\":14}";
        BudgetPlanRequests.UpdateBpBudgetRequest r =
                mapper.readValue(json, BudgetPlanRequests.UpdateBpBudgetRequest.class);
        assertThat(r.amountMinor()).isEqualTo(50000L);
        assertThat(r.frequencyUnit()).isEqualTo("months");
        assertThat(r.frequencyNumber()).isEqualTo(1);
        assertThat(r.accountId()).isEqualTo(14L);
    }

    @Test
    void updateBpBudgetRequest_deserializesLegacyTargetPerDayFieldName() throws Exception {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        String json =
                "{\"targetAmountPerDayMinor\":50000,\"frequencyUnit\":\"days\",\"frequencyNumber\":1,\"accountId\":14}";
        BudgetPlanRequests.UpdateBpBudgetRequest r =
                mapper.readValue(json, BudgetPlanRequests.UpdateBpBudgetRequest.class);
        assertThat(r.amountMinor()).isEqualTo(50000L);
    }
}
