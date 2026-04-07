package zachkingcade.dev.user.application.port.in.Session;

import zachkingcade.dev.user.adapter.persistence.jpa.UserSessionEntity;

public interface DeleteSessionUseCase {
    public void deleteSessionByEntity(UserSessionEntity entity);
}
