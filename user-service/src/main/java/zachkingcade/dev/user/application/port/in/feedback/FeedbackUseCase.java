package zachkingcade.dev.user.application.port.in.feedback;

import zachkingcade.dev.user.domain.feedback.UserFeedback;

import java.util.List;

public interface FeedbackUseCase {

    UserFeedback submit(long userId, String type, String title, String message);

    List<UserFeedback> listByType(String type);

    UserFeedback markSeen(long id);

    void delete(long id);
}
