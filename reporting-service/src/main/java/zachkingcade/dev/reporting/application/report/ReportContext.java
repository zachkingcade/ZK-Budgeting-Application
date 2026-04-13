package zachkingcade.dev.reporting.application.report;

import com.fasterxml.jackson.databind.JsonNode;
import zachkingcade.dev.reporting.adapter.outbound.LedgerApiClient;
import zachkingcade.dev.reporting.adapter.outbound.UserServiceTokenProvider;

public record ReportContext(
        long owningUserId,
        JsonNode parameters,
        UserServiceTokenProvider userServiceTokenProvider,
        LedgerApiClient ledgerApiClient
) {

    public String ledgerToken() {
        return userServiceTokenProvider.getLedgerAccessToken(owningUserId);
    }
}
