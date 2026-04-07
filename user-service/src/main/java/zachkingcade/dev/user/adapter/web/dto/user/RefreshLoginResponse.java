package zachkingcade.dev.user.adapter.web.dto.user;

import java.time.Instant;

public record RefreshLoginResponse(
        String accessToken,
        Instant accessTokenCreatedAt,
        Instant AccessTokenExpiresAt
) {
}
