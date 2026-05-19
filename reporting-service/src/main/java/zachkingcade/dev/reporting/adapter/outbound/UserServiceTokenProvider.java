package zachkingcade.dev.reporting.adapter.outbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches service JWTs keyed by audience set, scope set, and acting user id (never reuse across users).
 */
@Component
public class UserServiceTokenProvider {

    private static final List<String> DEFAULT_AUDIENCES = List.of("ledger-service");
    private static final List<String> DEFAULT_SCOPES = List.of(
            "ledger.accounts.read",
            "ledger.journalentries.read"
    );
    private static final List<String> NOTIFICATION_AUDIENCES = List.of("user-service");
    private static final List<String> NOTIFICATION_SCOPES = List.of("notifications.write");

    private final RestClient userServiceRestClient;
    private final ObjectMapper objectMapper;
    private final String serviceName;
    private final String serviceSecret;

    private final ConcurrentHashMap<CacheKey, CachedToken> cache = new ConcurrentHashMap<>();

    public UserServiceTokenProvider(
            RestClient userServiceRestClient,
            ObjectMapper objectMapper,
            @Value("${reporting.outbound.service-name}") String serviceName,
            @Value("${reporting.outbound.service-secret}") String serviceSecret
    ) {
        this.userServiceRestClient = userServiceRestClient;
        this.objectMapper = objectMapper;
        this.serviceName = serviceName;
        this.serviceSecret = serviceSecret;
    }

    public String getLedgerAccessToken(long actingForUserId) {
        return fetchToken(DEFAULT_AUDIENCES, DEFAULT_SCOPES, actingForUserId, true);
    }

    public String getNotificationsAccessToken() {
        return fetchToken(NOTIFICATION_AUDIENCES, NOTIFICATION_SCOPES, 0L, false);
    }

    private String fetchToken(List<String> audiences, List<String> scopes, long actingForUserId, boolean includeActingUser) {
        CacheKey key = new CacheKey(joinSorted(audiences), joinSorted(scopes), actingForUserId);
        Instant now = Instant.now();
        CachedToken cached = cache.get(key);
        if (cached != null && cached.expiresAt().isAfter(now.plusSeconds(30))) {
            return cached.token();
        }
        var requestBody = objectMapper.createObjectNode();
        requestBody.put("serviceName", serviceName);
        requestBody.put("secret", serviceSecret);
        if (includeActingUser) {
            requestBody.put("actingForUserId", actingForUserId);
        }
        requestBody.set("audiences", objectMapper.valueToTree(audiences));
        requestBody.set("scopes", objectMapper.valueToTree(scopes));
        String body = userServiceRestClient.post()
                .uri("/user/service/login")
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .retrieve()
                .body(String.class);
        try {
            JsonNode root = objectMapper.readTree(Objects.requireNonNull(body));
            JsonNode data = root.get("data");
            if (data == null || !data.has("accessToken")) {
                throw new IllegalStateException("Unexpected login response from user-service");
            }
            String token = data.get("accessToken").asText();
            Instant expiresAt = Instant.parse(data.get("accessTokenExpiresAt").asText());
            cache.put(key, new CachedToken(token, expiresAt));
            return token;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse service login response", e);
        }
    }

    private static String joinSorted(List<String> values) {
        return values.stream().sorted(Comparator.naturalOrder()).reduce((a, b) -> a + "|" + b).orElse("");
    }

    private record CacheKey(String audiencesKey, String scopesKey, long actingUserId) {}

    private record CachedToken(String token, Instant expiresAt) {}
}
