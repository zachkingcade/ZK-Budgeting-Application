package zachkingcade.dev.user.application.port.out.user;

import zachkingcade.dev.user.adapter.persistence.jpa.UserEntity;

import java.util.Optional;

public interface UserRepositoryPort {

    boolean existsByUsername(String username);

    UserEntity save(UserEntity entityToSave);

    Optional<UserEntity> getByUsername(String username);
}
