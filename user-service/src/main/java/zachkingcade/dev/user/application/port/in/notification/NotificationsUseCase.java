package zachkingcade.dev.user.application.port.in.notification;

import zachkingcade.dev.user.domain.notification.Notification;

import java.util.List;

public interface NotificationsUseCase {

    Notification register(Long userId, String system, String title, String message);

    List<Notification> listForUser(Long userId, Integer limit);

    List<Notification> listAdmin(Integer limit);

    Notification markSeen(Long userId, Long notificationId);

    void delete(Long userId, Long notificationId);

    void clearByIds(Long userId, List<Long> ids);

    void clearAll(Long userId);
}
