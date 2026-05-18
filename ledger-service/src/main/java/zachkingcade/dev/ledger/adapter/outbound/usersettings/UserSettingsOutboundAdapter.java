package zachkingcade.dev.ledger.adapter.outbound.usersettings;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import zachkingcade.dev.ledger.adapter.in.web.dto.ApiResponse;
import zachkingcade.dev.ledger.application.port.out.usersetting.UserSettingsClientPort;
import zachkingcade.dev.ledger.application.exception.ApplicationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserSettingsOutboundAdapter implements UserSettingsClientPort {

    private final RestClient userServiceRestClient;

    public UserSettingsOutboundAdapter(RestClient userServiceRestClient) {
        this.userServiceRestClient = userServiceRestClient;
    }

    @Override
    public Map<String, String> fetchSettingsByName(Long userId) {
        String bearer = resolveBearerToken();
        ApiResponse<List<UserSettingRemoteDto>> response = userServiceRestClient.get()
                .uri("/user/settings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        Map<String, String> byName = new HashMap<>();
        if (response != null && response.getData() != null) {
            for (UserSettingRemoteDto dto : response.getData()) {
                byName.put(dto.settingName(), dto.settingValue());
            }
        }
        return byName;
    }

    private static String resolveBearerToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }
        throw new ApplicationException("Authenticated JWT is required to load user settings");
    }

    record UserSettingRemoteDto(Long settingId, String settingName, String settingValue) {
    }
}
