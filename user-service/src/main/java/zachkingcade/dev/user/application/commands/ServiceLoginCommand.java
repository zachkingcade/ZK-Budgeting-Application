package zachkingcade.dev.user.application.commands;

import java.util.List;

public record ServiceLoginCommand(
        String serviceName,
        String secret,
        Long actingForUserId,
        List<String> requestedAudiences,
        List<String> requestedScopes
) {}
