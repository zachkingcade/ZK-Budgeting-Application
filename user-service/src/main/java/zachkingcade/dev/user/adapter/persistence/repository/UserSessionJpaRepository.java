package zachkingcade.dev.user.adapter.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.user.adapter.persistence.jpa.UserSessionEntity;

import java.util.Optional;

public interface UserSessionJpaRepository extends JpaRepository<UserSessionEntity, Long> {

    Optional<UserSessionEntity> findBySessionToken(String sessionToken);
}
