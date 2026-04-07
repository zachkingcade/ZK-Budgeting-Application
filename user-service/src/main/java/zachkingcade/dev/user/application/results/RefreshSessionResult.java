package zachkingcade.dev.user.application.results;

import java.time.Instant;

public record RefreshSessionResult(
        Boolean sessionRefreshed,
        String accessToken,
        Instant accessTokenCreatedAt,
        Instant AccessTokenExpiresAt
) {
}
