package zachkingcade.dev.user.adapter.web.dto.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterNotificationRequest(
        @NotNull Long userId,
        @NotBlank String system,
        @NotBlank String title,
        @NotBlank String message
) {
}
