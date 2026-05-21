package zachkingcade.dev.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zachkingcade.dev.user.application.exception.NotFoundException;
import zachkingcade.dev.user.application.port.in.notification.NotificationsUseCase;
import zachkingcade.dev.user.application.port.out.notification.NotificationRepositoryPort;
import zachkingcade.dev.user.domain.notification.Notification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class NotificationsService implements NotificationsUseCase {

    private final NotificationRepositoryPort repository;
    private final RolesService rolesService;

    public NotificationsService(NotificationRepositoryPort repository, RolesService rolesService) {
        this.repository = repository;
        this.rolesService = rolesService;
    }

    @Override
    public Notification register(Long userId, String system, String title, String message) {
        Instant now = Instant.now();
        Notification notification = new Notification(null, userId, now, system, title, message, false);
        return repository.save(notification);
    }

    @Override
    public List<Notification> listForUser(Long userId, Integer limit) {
        List<Notification> merged = new ArrayList<>(repository.findByUserIdOrderByCreatedAtDesc(userId, null));
        if (rolesService.isAdmin(userId)) {
            merged.addAll(repository.findByUserIdOrderByCreatedAtDesc(Notification.ADMIN_USER_ID, null));
            merged.sort(Comparator.comparing(Notification::datetime).reversed());
        }
        if (limit != null && limit > 0 && merged.size() > limit) {
            return merged.subList(0, limit);
        }
        return merged;
    }

    @Override
    public List<Notification> listAdmin(Integer limit) {
        return repository.findByUserIdOrderByCreatedAtDesc(Notification.ADMIN_USER_ID, limit);
    }

    @Override
    public Notification markSeen(Long userId, Long notificationId) {
        Notification existing = findAccessibleNotification(userId, notificationId)
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
        Notification existing = findAccessibleNotification(userId, notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        repository.deleteByIdAndUserId(existing.id(), existing.userId());
    }

    @Override
    @Transactional
    public void clearByIds(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            findAccessibleNotification(userId, id).ifPresent(notification ->
                    repository.deleteByIdAndUserId(notification.id(), notification.userId()));
        }
    }

    @Override
    @Transactional
    public void clearAll(Long userId) {
        repository.deleteAllByUserId(userId);
    }

    private java.util.Optional<Notification> findAccessibleNotification(Long callerUserId, Long notificationId) {
        java.util.Optional<Notification> personal = repository.findByIdAndUserId(notificationId, callerUserId);
        if (personal.isPresent()) {
            return personal;
        }
        if (rolesService.isAdmin(callerUserId)) {
            return repository.findByIdAndUserId(notificationId, Notification.ADMIN_USER_ID);
        }
        return java.util.Optional.empty();
    }
}
