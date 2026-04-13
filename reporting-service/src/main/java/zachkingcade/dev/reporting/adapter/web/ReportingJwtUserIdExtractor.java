package zachkingcade.dev.reporting.adapter.web;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

public final class ReportingJwtUserIdExtractor {

    private ReportingJwtUserIdExtractor() {
    }

    public static Long userId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new AccessDeniedException("JWT subject is required");
        }
        return Long.valueOf(jwt.getSubject());
    }
}
