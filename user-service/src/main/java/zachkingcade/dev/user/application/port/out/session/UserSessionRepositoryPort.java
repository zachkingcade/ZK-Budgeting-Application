package zachkingcade.dev.user.application.port.out.session;

import zachkingcade.dev.user.adapter.persistence.jpa.UserSessionEntity;

import java.util.Optional;

public interface UserSessionRepositoryPort {

    Optional<UserSessionEntity> findBySessionToken(String sessionToken);

    UserSessionEntity save(UserSessionEntity entityToSave);

    void deleteUserSessionByEntity(UserSessionEntity entity);
}
