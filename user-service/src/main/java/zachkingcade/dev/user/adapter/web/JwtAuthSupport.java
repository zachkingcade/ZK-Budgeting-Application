package zachkingcade.dev.user.adapter.web;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class JwtAuthSupport {

    private JwtAuthSupport() {
    }

    public static Long requireUserId(Jwt jwt) {
        requireUserToken(jwt);
        return JwtPrincipalUserIdExtractor.extractUserId(jwt);
    }

    public static void requireUserToken(Jwt jwt) {
        if (!isUserToken(jwt)) {
            throw new AccessDeniedException("User access token required");
        }
    }

    public static void requireServiceToken(Jwt jwt) {
        if (!isServiceToken(jwt)) {
            throw new AccessDeniedException("Service access token required");
        }
    }

    public static void requireServiceScope(Jwt jwt, String requiredScope) {
        requireServiceToken(jwt);
        Set<String> scopes = parseScopes(jwt.getClaimAsString("scope"));
        if (!scopes.contains(requiredScope)) {
            throw new AccessDeniedException("Missing required scope: " + requiredScope);
        }
    }

    public static boolean isUserToken(Jwt jwt) {
        return jwt != null && "user".equals(jwt.getClaimAsString("token_type"));
    }

    public static boolean isServiceToken(Jwt jwt) {
        return jwt != null && "service".equals(jwt.getClaimAsString("token_type"));
    }

    private static Set<String> parseScopes(String scopeClaim) {
        if (scopeClaim == null || scopeClaim.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(scopeClaim.split("\\s+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
