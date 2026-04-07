package zachkingcade.dev.user.application.results;

import java.time.Instant;

public record LogInUserResult(
        String username,
        String sessionToken,
        Instant sessionCreatedAt,
        Instant sessionExpiresAt,
        String accessToken,
        Instant accessTokenCreatedAt,
        Instant AccessTokenExpiresAt
) {
}
