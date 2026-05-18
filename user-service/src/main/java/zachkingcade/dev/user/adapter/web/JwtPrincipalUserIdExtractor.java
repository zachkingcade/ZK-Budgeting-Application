package zachkingcade.dev.user.adapter.web;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

public final class JwtPrincipalUserIdExtractor {

    private JwtPrincipalUserIdExtractor() {
    }

    public static Long extractUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new AccessDeniedException("JWT subject is required");
        }
        return Long.valueOf(jwt.getSubject());
    }
}
