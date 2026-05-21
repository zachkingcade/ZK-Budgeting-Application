package zachkingcade.dev.user.adapter.web.dto.feedback;

import zachkingcade.dev.user.domain.feedback.UserFeedback;

import java.time.Instant;

public record FeedbackResponse(
        Long id,
        String type,
        String title,
        String message,
        Long userId,
        String username,
        Instant createdAt,
        boolean seen
) {
    public static FeedbackResponse from(UserFeedback feedback) {
        return new FeedbackResponse(
                feedback.id(),
                feedback.type(),
                feedback.title(),
                feedback.message(),
                feedback.userId(),
                feedback.username(),
                feedback.createdAt(),
                feedback.seen());
    }
}
