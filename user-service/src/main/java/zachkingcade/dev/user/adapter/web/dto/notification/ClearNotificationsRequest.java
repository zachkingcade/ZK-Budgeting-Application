package zachkingcade.dev.user.adapter.web.dto.notification;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ClearNotificationsRequest(
        @NotEmpty List<Long> ids
) {
}
