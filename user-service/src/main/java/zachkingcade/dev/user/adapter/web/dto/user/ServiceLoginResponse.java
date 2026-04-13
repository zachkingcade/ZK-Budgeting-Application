package zachkingcade.dev.user.adapter.web.dto.user;

import java.time.Instant;

public record ServiceLoginResponse(
        String accessToken,
        Instant accessTokenCreatedAt,
        Instant accessTokenExpiresAt
) {}
