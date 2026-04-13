package zachkingcade.dev.ledger.adapter.in.web;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

public final class JwtPrincipalUserIdExtractor {

    private JwtPrincipalUserIdExtractor() {
    }

    public static Long extractEffectiveUserId(Jwt jwt) {
        if (jwt == null) {
            throw new AccessDeniedException("JWT is required");
        }
        String tokenType = jwt.hasClaim("token_type") ? jwt.getClaimAsString("token_type") : "user";
        if ("service".equals(tokenType)) {
            Object claim = jwt.getClaim("acting_for_user_id");
            if (claim == null) {
                throw new AccessDeniedException("Service JWT must include acting_for_user_id for this operation");
            }
            if (claim instanceof Number n) {
                return n.longValue();
            }
            if (claim instanceof String s && !s.isBlank()) {
                return Long.valueOf(s);
            }
            throw new AccessDeniedException("Invalid acting_for_user_id claim");
        }
        if (jwt.getSubject() == null) {
            throw new AccessDeniedException("JWT subject is required");
        }
        return Long.valueOf(jwt.getSubject());
    }
}
