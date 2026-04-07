package zachkingcade.dev.user.application;

import org.springframework.stereotype.Service;
import zachkingcade.dev.user.adapter.persistence.jpa.UserEntity;
import zachkingcade.dev.user.adapter.persistence.jpa.UserSessionEntity;
import zachkingcade.dev.user.application.commands.CreateSessionCommand;
import zachkingcade.dev.user.application.port.in.Session.CreateUserSessionUseCase;
import zachkingcade.dev.user.application.port.in.Session.DeleteSessionUseCase;
import zachkingcade.dev.user.application.port.in.Session.FindSessionBySessionTokenUseCase;
import zachkingcade.dev.user.application.port.out.session.UserSessionRepositoryPort;
import zachkingcade.dev.user.domain.session.Session;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class SessionService implements CreateUserSessionUseCase, FindSessionBySessionTokenUseCase, DeleteSessionUseCase {

    UserSessionRepositoryPort userSessionRepository;

    final private Long sessionTimeInSeconds = 86400L;

    public SessionService(UserSessionRepositoryPort userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    @Override
    public Session createSession(CreateSessionCommand command) {
        // Create session
        String sessionToken = java.util.UUID.randomUUID().toString();
        Instant created = Instant.now();
        Instant expires = created.plus(sessionTimeInSeconds, ChronoUnit.SECONDS);
        Session session = Session.createNew(command.user().getUsername(),sessionToken,created,expires);

        // Persist Session
        UserSessionEntity entity = domainToEntity(session, command.user());
        UserSessionEntity saved = saveSession(entity);

        return session.withId(saved.getId());
    }

    public Session saveSession(Session sessionToSave, UserEntity userEntity){
        UserSessionEntity saved = this.saveSession(domainToEntity(sessionToSave, userEntity));
        return sessionToSave.withId(saved.getId());
    }

    public UserSessionEntity saveSession(UserSessionEntity sessionToSave){
        return this.userSessionRepository.save(sessionToSave);
    }

    private UserSessionEntity domainToEntity(Session session, UserEntity userEntity){
        UserSessionEntity resultingEntity = new UserSessionEntity();
        resultingEntity.setUser(userEntity);
        resultingEntity.setSessionToken(session.getSessionToken());
        resultingEntity.setCreatedDate(session.getCreated());
        resultingEntity.setExpiresDate(session.getExpires());
        return resultingEntity;
    }

    public Optional<UserSessionEntity> findBySessionToken(String sessionToken){
        return this.userSessionRepository.findBySessionToken(sessionToken);
    }

    public void deleteSessionByEntity(UserSessionEntity entity){
        this.userSessionRepository.deleteUserSessionByEntity(entity);
    }
}
