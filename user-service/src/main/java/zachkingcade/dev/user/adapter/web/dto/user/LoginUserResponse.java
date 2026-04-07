package zachkingcade.dev.user.adapter.web.dto.user;

import java.time.Instant;

public record LoginUserResponse(
    String username,
    String sessionToken,
    Instant sessionCreatedAt,
    Instant sessionExpiresAt,
    String accessToken,
    Instant accessTokenCreatedAt,
    Instant AccessTokenExpiresAt
) {
}
