package zachkingcade.dev.user.adapter.persistence;

import org.springframework.stereotype.Service;
import zachkingcade.dev.user.adapter.persistence.jpa.UserSessionEntity;
import zachkingcade.dev.user.adapter.persistence.repository.UserSessionJpaRepository;
import zachkingcade.dev.user.application.port.out.session.UserSessionRepositoryPort;

import java.util.Optional;

@Service
public class UserSessionPersistenceAdapter implements UserSessionRepositoryPort {

    UserSessionJpaRepository userSessionRepository;

    public UserSessionPersistenceAdapter(UserSessionJpaRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    @Override
    public Optional<UserSessionEntity> findBySessionToken(String sessionToken) {
        return this.userSessionRepository.findBySessionToken(sessionToken);
    }

    @Override
    public UserSessionEntity save(UserSessionEntity entityToSave) {
        return this.userSessionRepository.save(entityToSave);
    }

    @Override
    public void deleteUserSessionByEntity(UserSessionEntity entity) {
        this.userSessionRepository.delete(entity);
    }
}
