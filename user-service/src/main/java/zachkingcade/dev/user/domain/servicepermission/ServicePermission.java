package zachkingcade.dev.user.domain.servicepermission;

import java.time.Instant;
import java.util.List;

public record ServicePermission(
        Long id,
        String serviceName,
        String secretHash,
        List<String> allowedAudiences,
        List<String> allowedScopes,
        boolean mayActForUser,
        Instant createdDate
) {}
