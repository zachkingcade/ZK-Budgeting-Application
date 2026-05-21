package zachkingcade.dev.user.application.port.out.feedback;

import zachkingcade.dev.user.domain.feedback.UserFeedback;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepositoryPort {

    UserFeedback save(UserFeedback feedback, long userId);

    List<UserFeedback> findByTypeOrderByCreatedAtDesc(String type);

    Optional<UserFeedback> findById(long id);

    void deleteById(long id);
}
