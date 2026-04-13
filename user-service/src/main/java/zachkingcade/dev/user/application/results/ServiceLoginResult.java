package zachkingcade.dev.user.application.results;

import java.time.Instant;

public record ServiceLoginResult(
        String accessToken,
        Instant accessTokenCreatedAt,
        Instant accessTokenExpiresAt
) {}
