package zachkingcade.dev.reporting.adapter.outbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserServiceNotificationClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceNotificationClient.class);

    private final RestClient userServiceRestClient;
    private final UserServiceTokenProvider userServiceTokenProvider;
    private final ObjectMapper objectMapper;

    public UserServiceNotificationClient(
            RestClient userServiceRestClient,
            UserServiceTokenProvider userServiceTokenProvider,
            ObjectMapper objectMapper
    ) {
        this.userServiceRestClient = userServiceRestClient;
        this.userServiceTokenProvider = userServiceTokenProvider;
        this.objectMapper = objectMapper;
    }

    public void registerReportReady(long userId, String reportType) {
        try {
            String token = userServiceTokenProvider.getNotificationsAccessToken();
            ObjectNode body = objectMapper.createObjectNode();
            body.put("userId", userId);
            body.put("system", "reports");
            body.put("title", "Report ready");
            body.put("message", "Your " + reportType + " report is ready to download.");
            userServiceRestClient.post()
                    .uri("/user/notifications/register")
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to register report-ready notification for userId={} reportType={}", userId, reportType, e);
        }
    }
}
