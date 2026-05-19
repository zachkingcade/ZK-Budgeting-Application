package zachkingcade.dev.user.adapter.web.dto.notification;

import zachkingcade.dev.user.domain.notification.Notification;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long userId,
        Instant datetime,
        String system,
        String title,
        String message,
        boolean seen
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.id(),
                notification.userId(),
                notification.datetime(),
                notification.system(),
                notification.title(),
                notification.message(),
                notification.seen()
        );
    }
}
