package zachkingcade.dev.user.adapter.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.user.adapter.persistence.jpa.UserFeedbackEntity;

import java.util.List;

public interface UserFeedbackJpaRepository extends JpaRepository<UserFeedbackEntity, Long> {

    List<UserFeedbackEntity> findByTypeOrderByCreatedAtDesc(String type);
}
