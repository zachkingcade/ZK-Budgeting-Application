package zachkingcade.dev.ledger.adapter.in.web.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enforces read-only + scope rules for {@code token_type=service} JWTs. User tokens are unchanged.
 */
@Component
public class LedgerJwtAuthorizationFilter extends OncePerRequestFilter {

    public static final String SCOPE_ACCOUNTS_READ = "ledger.accounts.read";
    public static final String SCOPE_JOURNAL_READ = "ledger.journalentries.read";

    private static final UrlPathHelper PATH_HELPER = new UrlPathHelper();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            filterChain.doFilter(request, response);
            return;
        }
        Object principal = jwtAuth.getPrincipal();
        if (!(principal instanceof Jwt jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        String tokenType = jwt.hasClaim("token_type") ? jwt.getClaimAsString("token_type") : "user";
        if (!"service".equals(tokenType)) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = PATH_HELPER.getPathWithinApplication(request);
        if (path != null && path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        String method = request.getMethod().toUpperCase(Locale.ROOT);

        if (isServiceForbidden(method, path)) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "Service tokens cannot access mutating endpoints");
            return;
        }

        if (!isAllowedServiceRead(method, path)) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "Endpoint not allowed for service tokens");
            return;
        }

        Set<String> scopes = parseScopes(jwt);
        if (requiresAccountsRead(method, path) && !scopes.contains(SCOPE_ACCOUNTS_READ)) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "Missing required scope: " + SCOPE_ACCOUNTS_READ);
            return;
        }
        if (requiresJournalRead(method, path) && !scopes.contains(SCOPE_JOURNAL_READ)) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "Missing required scope: " + SCOPE_JOURNAL_READ);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static boolean isServiceForbidden(String method, String path) {
        if ("POST".equals(method)) {
            return path.equals("/accounts/add")
                    || path.equals("/accounts/update")
                    || path.equals("/accounttypes/add")
                    || path.equals("/accounttypes/update")
                    || path.equals("/journalentry/add")
                    || path.equals("/journalentry/update");
        }
        return "DELETE".equals(method) && path.startsWith("/journalentry/remove");
    }

    private static boolean isAllowedServiceRead(String method, String path) {
        if (matchesAccountsOrTypesOrClassifications(method, path)) {
            return true;
        }
        return matchesJournalRead(method, path);
    }

    private static boolean matchesAccountsOrTypesOrClassifications(String method, String path) {
        if (path.startsWith("/accounts")) {
            return method.equals("GET") || (method.equals("POST") && path.equals("/accounts/all/filtered"));
        }
        if (path.startsWith("/accounttypes")) {
            return method.equals("GET") || (method.equals("POST") && path.equals("/accounttypes/all/filtered"));
        }
        if (path.startsWith("/accountclassifications")) {
            return method.equals("GET");
        }
        return false;
    }

    private static boolean matchesJournalRead(String method, String path) {
        if (!path.startsWith("/journalentry")) {
            return false;
        }
        if (method.equals("GET")) {
            return true;
        }
        return method.equals("POST") && path.equals("/journalentry/all/filtered");
    }

    private static boolean requiresAccountsRead(String method, String path) {
        if (path.startsWith("/accounts") || path.startsWith("/accounttypes") || path.startsWith("/accountclassifications")) {
            return true;
        }
        return false;
    }

    private static boolean requiresJournalRead(String method, String path) {
        return path.startsWith("/journalentry");
    }

    private static Set<String> parseScopes(Jwt jwt) {
        if (!jwt.hasClaim("scope")) {
            return Set.of();
        }
        Object raw = jwt.getClaim("scope");
        if (raw instanceof String s) {
            return Arrays.stream(s.split("\\s+"))
                    .map(String::trim)
                    .filter(x -> !x.isEmpty())
                    .collect(Collectors.toCollection(HashSet::new));
        }
        if (raw instanceof Iterable<?> it) {
            Set<String> out = new HashSet<>();
            for (Object o : it) {
                if (o != null) {
                    out.add(o.toString());
                }
            }
            return out;
        }
        return Set.of();
    }
}
