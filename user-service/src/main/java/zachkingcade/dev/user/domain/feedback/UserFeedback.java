package zachkingcade.dev.user.domain.feedback;

import java.time.Instant;

public record UserFeedback(
        Long id,
        String type,
        String title,
        String message,
        Long userId,
        String username,
        Instant createdAt,
        boolean seen
) {
    public static final String TYPE_BUG = "bug";
    public static final String TYPE_SUGGESTION = "suggestion";
}
