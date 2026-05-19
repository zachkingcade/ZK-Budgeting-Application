package zachkingcade.dev.user.domain.notification;

import java.time.Instant;

public record Notification(
        Long id,
        Long userId,
        Instant datetime,
        String system,
        String title,
        String message,
        boolean seen
) {
    public static final long ADMIN_USER_ID = -1L;
}
