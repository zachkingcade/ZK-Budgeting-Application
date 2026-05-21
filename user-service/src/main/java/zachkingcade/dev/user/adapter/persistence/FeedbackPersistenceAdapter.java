package zachkingcade.dev.user.adapter.persistence;

import org.springframework.stereotype.Service;
import zachkingcade.dev.user.adapter.persistence.jpa.UserEntity;
import zachkingcade.dev.user.adapter.persistence.jpa.UserFeedbackEntity;
import zachkingcade.dev.user.adapter.persistence.repository.UserFeedbackJpaRepository;
import zachkingcade.dev.user.adapter.persistence.repository.UserJpaRepository;
import zachkingcade.dev.user.application.exception.NotFoundException;
import zachkingcade.dev.user.application.port.out.feedback.FeedbackRepositoryPort;
import zachkingcade.dev.user.domain.feedback.UserFeedback;

import java.util.List;
import java.util.Optional;

@Service
public class FeedbackPersistenceAdapter implements FeedbackRepositoryPort {

    private final UserFeedbackJpaRepository feedbackRepository;
    private final UserJpaRepository userRepository;

    public FeedbackPersistenceAdapter(
            UserFeedbackJpaRepository feedbackRepository,
            UserJpaRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    @Override
    public UserFeedback save(UserFeedback feedback, long userId) {
        UserFeedbackEntity entity;
        if (feedback.id() != null) {
            entity = feedbackRepository.findById(feedback.id())
                    .orElseThrow(() -> new NotFoundException("Feedback not found"));
        } else {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            entity = new UserFeedbackEntity();
            entity.setUser(user);
            entity.setCreatedAt(feedback.createdAt());
        }
        entity.setType(feedback.type());
        entity.setTitle(feedback.title());
        entity.setMessage(feedback.message());
        entity.setSeen(feedback.seen());

        UserFeedbackEntity saved = feedbackRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<UserFeedback> findByTypeOrderByCreatedAtDesc(String type) {
        return feedbackRepository.findByTypeOrderByCreatedAtDesc(type).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<UserFeedback> findById(long id) {
        return feedbackRepository.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(long id) {
        feedbackRepository.deleteById(id);
    }

    private UserFeedback toDomain(UserFeedbackEntity entity) {
        return new UserFeedback(
                entity.getId(),
                entity.getType(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getUserId(),
                entity.getUser().getUsername(),
                entity.getCreatedAt(),
                entity.isSeen());
    }
}
