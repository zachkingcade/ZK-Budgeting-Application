package zachkingcade.dev.user.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import zachkingcade.dev.user.adapter.persistence.jpa.UserEntity;
import zachkingcade.dev.user.adapter.persistence.jpa.UserSessionEntity;
import zachkingcade.dev.user.application.commands.*;
import zachkingcade.dev.user.application.exception.ApplicationException;
import zachkingcade.dev.user.application.exception.NotFoundException;
import zachkingcade.dev.user.application.port.in.Session.CreateUserSessionUseCase;
import zachkingcade.dev.user.application.port.in.Session.DeleteSessionUseCase;
import zachkingcade.dev.user.application.port.in.Session.FindSessionBySessionTokenUseCase;
import zachkingcade.dev.user.application.port.in.Session.RefreshSessionUseCase;
import zachkingcade.dev.user.application.port.in.user.LoginUserUseCase;
import zachkingcade.dev.user.application.port.in.user.LogoutUserUseCase;
import zachkingcade.dev.user.application.port.in.user.RegisterUserUseCase;
import zachkingcade.dev.user.application.port.out.user.UserRepositoryPort;
import zachkingcade.dev.user.application.results.LogInUserResult;
import zachkingcade.dev.user.application.results.RefreshSessionResult;
import zachkingcade.dev.user.domain.session.Session;
import zachkingcade.dev.user.domain.user.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class UserService implements RegisterUserUseCase, LoginUserUseCase, LogoutUserUseCase, RefreshSessionUseCase {

    UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    CreateUserSessionUseCase createUserSessionUseCase;
    FindSessionBySessionTokenUseCase findSessionBySessionTokenUseCase;
    DeleteSessionUseCase deleteSessionUseCase;
    JWTService jwtService;

    public UserService(
            UserRepositoryPort userRepository,
            PasswordEncoder passwordEncoder,
            CreateUserSessionUseCase createUserSessionUseCase,
            JWTService jwtService,
            FindSessionBySessionTokenUseCase findSessionBySessionTokenUseCase,
            DeleteSessionUseCase deleteSessionUseCase
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.createUserSessionUseCase = createUserSessionUseCase;
        this.jwtService = jwtService;
        this.findSessionBySessionTokenUseCase = findSessionBySessionTokenUseCase;
        this.deleteSessionUseCase = deleteSessionUseCase;
    }

    @Override
    public User registerUser(RegisterUserCommand command) {
        // Check that this username does not already exist
        if(userRepository.existsByUsername(command.username())){
            throw new ApplicationException(String.format("Username [%s] is already taken", command.username()));
        }

        Instant currentTime = Instant.now();

        //create domain object
        User newUser = User.createNew(command.username(), command.password(), true, currentTime, currentTime);

        //encrypt password and save entity
        String hashedPassword = passwordEncoder.encode(command.password());
        UserEntity entity = new UserEntity();
        entity.setUsername(newUser.getUsername());
        entity.setPasswordHash(hashedPassword);
        entity.setActive(newUser.getActive());
        entity.setCreatedDate(newUser.getCreated_date());
        entity.setUpdatedDate(newUser.getUpdated_date());
        UserEntity saved = userRepository.save(entity);


        return newUser.withId(saved.getUserId());
    }

    @Override
    public LogInUserResult loginUser(LoginUserCommand command) {
        // Find user
        UserEntity userEntity = userRepository.getByUsername(command.username()).orElseThrow( () ->
                new NotFoundException(String.format("User with the username [%s] not found", command.username()))
        );

        // Validate password
        boolean passwordMatches = passwordEncoder.matches(command.password(), userEntity.getPasswordHash());
        if(!passwordMatches){
            throw new ApplicationException("Password does not match data in our system.");
        }

        // Create Access Token
        String accessToken = jwtService.generateAccessToken(userEntity.getUserId(), userEntity.getUsername());
        // TODO put this in JWTService and abstract the response with a result object
        Instant accessTokenCreated = Instant.now();
        Instant accessTokenExpires = accessTokenCreated.plus(10, ChronoUnit.MINUTES);

        // Create Session
        CreateSessionCommand sessionCommand = new CreateSessionCommand(userEntity);
        Session session = this.createUserSessionUseCase.createSession(sessionCommand);

        return new LogInUserResult(userEntity.getUsername(), session.getSessionToken(), session.getCreated(), session.getExpires(), accessToken, accessTokenCreated, accessTokenExpires);
    }
    
    

    @Override
    public void logoutUser(LogoutUserCommand command) {
        // Find user
        UserEntity userEntity = userRepository.getByUsername(command.username()).orElseThrow( () ->
                new NotFoundException(String.format("User with the username [%s] not found", command.username()))
        );

        // Find Session
        UserSessionEntity sessionEntity = findSessionBySessionTokenUseCase.findBySessionToken(command.sessionToken()).orElseThrow( () ->
                new NotFoundException(String.format("Session with the session token [%s] not found", command.username()))
        );

        if(!userEntity.getUsername().equals(sessionEntity.getUser().getUsername())){
            throw new ApplicationException("Session's stored User does not match the provided User.");
        }

        // Delete user
        deleteSessionUseCase.deleteSessionByEntity(sessionEntity);
    }

    @Override
    public RefreshSessionResult refreshSession(RefreshSessionCommand command) {
        // Find user
        UserEntity userEntity = userRepository.getByUsername(command.username()).orElseThrow( () ->
                new NotFoundException(String.format("User with the username [%s] not found", command.username()))
        );

        // Find Session
        UserSessionEntity sessionEntity = findSessionBySessionTokenUseCase.findBySessionToken(command.sessionToken()).orElseThrow( () ->
                new NotFoundException(String.format("Session with the session token [%s] not found", command.username()))
        );

        if(!userEntity.getUsername().equals(sessionEntity.getUser().getUsername())){
            throw new ApplicationException("Session's stored User does not match the provided User.");
        }

        if(sessionEntity.getExpiresDate().isBefore(Instant.now())){
            return new RefreshSessionResult(false,null,null, null);
        }

        // Create new Access Token
        String accessToken = jwtService.generateAccessToken(userEntity.getUserId(), userEntity.getUsername());
        // TODO put this in JWTService and abstract the response with a result object
        Instant accessTokenCreated = Instant.now();
        Instant accessTokenExpires = accessTokenCreated.plus(10, ChronoUnit.MINUTES);

        return new RefreshSessionResult(true, accessToken, accessTokenCreated, accessTokenExpires);
    }
}
