package zachkingcade.dev.user.adapter.web.dto.user;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ServiceLoginRequest(
        @NotBlank String serviceName,
        @NotBlank String secret,
        Long actingForUserId,
        List<String> audiences,
        List<String> scopes
) {}
