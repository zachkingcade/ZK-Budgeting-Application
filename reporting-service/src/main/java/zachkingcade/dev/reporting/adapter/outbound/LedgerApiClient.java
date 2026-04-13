package zachkingcade.dev.reporting.adapter.outbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Component
public class LedgerApiClient {

    private final RestClient ledgerRestClient;
    private final ObjectMapper objectMapper;

    public LedgerApiClient(RestClient ledgerRestClient, ObjectMapper objectMapper) {
        this.ledgerRestClient = ledgerRestClient;
        this.objectMapper = objectMapper;
    }

    public JsonNode getAccountsAll(String bearerToken) {
        return unwrapData(get("/accounts/all", bearerToken, null));
    }

    public JsonNode postAccountsFiltered(String bearerToken, JsonNode body) {
        return unwrapData(post("/accounts/all/filtered", bearerToken, body));
    }

    public JsonNode getAccountTypesAll(String bearerToken) {
        return unwrapData(get("/accounttypes/all", bearerToken, null));
    }

    public JsonNode postAccountTypesFiltered(String bearerToken, JsonNode body) {
        return unwrapData(post("/accounttypes/all/filtered", bearerToken, body));
    }

    public JsonNode getAccountClassificationsAll(String bearerToken) {
        return unwrapData(get("/accountclassifications/all", bearerToken, null));
    }

    public JsonNode postJournalFiltered(String bearerToken, JsonNode body) {
        return unwrapData(post("/journalentry/all/filtered", bearerToken, body));
    }

    private String get(String path, String bearerToken, JsonNode ignored) {
        return Objects.requireNonNull(ledgerRestClient.get()
                .uri(path)
                .header("Authorization", "Bearer " + bearerToken)
                .retrieve()
                .body(String.class));
    }

    private String post(String path, String bearerToken, JsonNode body) {
        var spec = ledgerRestClient.post()
                .uri(path)
                .header("Authorization", "Bearer " + bearerToken)
                .header("Content-Type", "application/json");
        if (body != null) {
            spec.body(body.toString());
        }
        return Objects.requireNonNull(spec.retrieve().body(String.class));
    }

    private JsonNode unwrapData(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.get("data");
            return data != null ? data : objectMapper.createObjectNode();
        } catch (Exception e) {
            throw new IllegalStateException("Invalid ledger JSON response", e);
        }
    }
}
