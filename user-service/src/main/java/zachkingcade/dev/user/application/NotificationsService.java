package zachkingcade.dev.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zachkingcade.dev.user.application.exception.NotFoundException;
import zachkingcade.dev.user.application.port.in.notification.NotificationsUseCase;
import zachkingcade.dev.user.application.port.out.notification.NotificationRepositoryPort;
import zachkingcade.dev.user.domain.notification.Notification;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationsService implements NotificationsUseCase {

    private final NotificationRepositoryPort repository;

    public NotificationsService(NotificationRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Notification register(Long userId, String system, String title, String message) {
        Instant now = Instant.now();
        Notification notification = new Notification(null, userId, now, system, title, message, false);
        return repository.save(notification);
    }

    @Override
    public List<Notification> listForUser(Long userId, Integer limit) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId, limit);
    }

    @Override
    public List<Notification> listAdmin(Integer limit) {
        return repository.findByUserIdOrderByCreatedAtDesc(Notification.ADMIN_USER_ID, limit);
    }

    @Override
    public Notification markSeen(Long userId, Long notificationId) {
        Notification existing = repository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        if (existing.seen()) {
            return existing;
        }
        Notification updated = new Notification(
                existing.id(),
                existing.userId(),
                existing.datetime(),
                existing.system(),
                existing.title(),
                existing.message(),
                true);
        return repository.save(updated);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long notificationId) {
        if (repository.findByIdAndUserId(notificationId, userId).isEmpty()) {
            throw new NotFoundException("Notification not found");
        }
        repository.deleteByIdAndUserId(notificationId, userId);
    }

    @Override
    @Transactional
    public void clearByIds(Long userId, List<Long> ids) {
        repository.deleteByIdsAndUserId(ids, userId);
    }

    @Override
    @Transactional
    public void clearAll(Long userId) {
        repository.deleteAllByUserId(userId);
    }
}
