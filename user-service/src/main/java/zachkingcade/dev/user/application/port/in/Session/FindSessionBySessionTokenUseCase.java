package zachkingcade.dev.user.application.port.in.Session;

import zachkingcade.dev.user.adapter.persistence.jpa.UserSessionEntity;

import java.util.Optional;

public interface FindSessionBySessionTokenUseCase {
    public Optional<UserSessionEntity> findBySessionToken(String sessionToken);
}
