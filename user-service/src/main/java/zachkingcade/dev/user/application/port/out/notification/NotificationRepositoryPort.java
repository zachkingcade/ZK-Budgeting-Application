package zachkingcade.dev.user.application.port.out.notification;

import zachkingcade.dev.user.domain.notification.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepositoryPort {

    Notification save(Notification notification);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Integer limit);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);

    void deleteByIdsAndUserId(List<Long> ids, Long userId);

    void deleteAllByUserId(Long userId);
}
