package zachkingcade.dev.user.adapter.persistence;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import zachkingcade.dev.user.adapter.persistence.jpa.NotificationEntity;
import zachkingcade.dev.user.adapter.persistence.repository.NotificationJpaRepository;
import zachkingcade.dev.user.application.port.out.notification.NotificationRepositoryPort;
import zachkingcade.dev.user.domain.notification.Notification;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationPersistenceAdapter implements NotificationRepositoryPort {

    private final NotificationJpaRepository repository;

    public NotificationPersistenceAdapter(NotificationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity = new NotificationEntity();
        if (notification.id() != null) {
            entity.setId(notification.id());
        }
        entity.setUserId(notification.userId());
        entity.setCreatedAt(notification.datetime());
        entity.setSourceSystem(notification.system());
        entity.setTitle(notification.title());
        entity.setMessage(notification.message());
        entity.setSeen(notification.seen());
        return toDomain(repository.save(entity));
    }

    @Override
    public List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Integer limit) {
        var pageable = limit != null && limit > 0
                ? PageRequest.of(0, limit)
                : PageRequest.of(0, Integer.MAX_VALUE);
        return repository.findByUserIdOrderByCreatedAtDesc(userId, pageable).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Notification> findByIdAndUserId(Long id, Long userId) {
        return repository.findByIdAndUserId(id, userId).map(this::toDomain);
    }

    @Override
    public void deleteByIdAndUserId(Long id, Long userId) {
        repository.deleteByIdAndUserId(id, userId);
    }

    @Override
    public void deleteByIdsAndUserId(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        repository.deleteByUserIdAndIdIn(userId, ids);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        repository.deleteAllByUserId(userId);
    }

    private Notification toDomain(NotificationEntity entity) {
        return new Notification(
                entity.getId(),
                entity.getUserId(),
                entity.getCreatedAt(),
                entity.getSourceSystem(),
                entity.getTitle(),
                entity.getMessage(),
                entity.isSeen()
        );
    }
}
