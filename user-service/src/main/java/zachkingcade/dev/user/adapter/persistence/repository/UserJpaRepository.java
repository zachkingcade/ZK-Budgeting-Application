package zachkingcade.dev.user.adapter.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.user.adapter.persistence.jpa.UserEntity;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}
