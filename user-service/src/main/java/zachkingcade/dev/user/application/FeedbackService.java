package zachkingcade.dev.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zachkingcade.dev.user.application.exception.ApplicationException;
import zachkingcade.dev.user.application.exception.NotFoundException;
import zachkingcade.dev.user.application.port.in.feedback.FeedbackUseCase;
import zachkingcade.dev.user.application.port.in.notification.NotificationsUseCase;
import zachkingcade.dev.user.application.port.out.feedback.FeedbackRepositoryPort;
import zachkingcade.dev.user.application.port.out.user.UserRepositoryPort;
import zachkingcade.dev.user.domain.feedback.UserFeedback;
import zachkingcade.dev.user.domain.notification.Notification;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class FeedbackService implements FeedbackUseCase {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            UserFeedback.TYPE_BUG,
            UserFeedback.TYPE_SUGGESTION);

    private final FeedbackRepositoryPort feedbackRepository;
    private final NotificationsUseCase notificationsUseCase;
    private final UserRepositoryPort userRepository;

    public FeedbackService(
            FeedbackRepositoryPort feedbackRepository,
            NotificationsUseCase notificationsUseCase,
            UserRepositoryPort userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.notificationsUseCase = notificationsUseCase;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserFeedback submit(long userId, String type, String title, String message) {
        validateType(type);
        validateText(title, "Title");
        validateText(message, "Message");

        String username = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User not found"))
                .getUsername();

        Instant now = Instant.now();
        UserFeedback draft = new UserFeedback(null, type, title.trim(), message.trim(), userId, username, now, false);
        UserFeedback saved = feedbackRepository.save(draft, userId);

        String notificationTitle = UserFeedback.TYPE_BUG.equals(type)
                ? "New bug report"
                : "New suggestion";
        String notificationMessage = String.format("%s: %s", username, title.trim());

        notificationsUseCase.register(
                Notification.ADMIN_USER_ID,
                "user-service",
                notificationTitle,
                notificationMessage);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserFeedback> listByType(String type) {
        validateType(type);
        return feedbackRepository.findByTypeOrderByCreatedAtDesc(type);
    }

    @Override
    @Transactional
    public UserFeedback markSeen(long id) {
        UserFeedback existing = feedbackRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Feedback not found"));
        if (existing.seen()) {
            return existing;
        }
        UserFeedback updated = new UserFeedback(
                existing.id(),
                existing.type(),
                existing.title(),
                existing.message(),
                existing.userId(),
                existing.username(),
                existing.createdAt(),
                true);
        return feedbackRepository.save(updated, existing.userId());
    }

    @Override
    @Transactional
    public void delete(long id) {
        if (feedbackRepository.findById(id).isEmpty()) {
            throw new NotFoundException("Feedback not found");
        }
        feedbackRepository.deleteById(id);
    }

    private static void validateType(String type) {
        if (type == null || !ALLOWED_TYPES.contains(type)) {
            throw new ApplicationException("Type must be bug or suggestion");
        }
    }

    private static void validateText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new ApplicationException(label + " is required");
        }
    }
}
