package zachkingcade.dev.user.domain.session;

import zachkingcade.dev.user.domain.exception.DomainException;
import zachkingcade.dev.user.domain.user.User;

import java.time.Duration;
import java.time.Instant;

public class Session {
    private final Long id;
    private final String username;
    private final String sessionToken;
    private final Instant created;
    private final Instant expires;

    private Session(Long id, String username, String sessionToken, Instant created, Instant expires) {
        if(Duration.between(created,expires).getSeconds() <= 60){
            throw new DomainException("Session must expire at least 60 seconds after session starts.");
        }

        this.id = id;
        this.username = username;
        this.sessionToken = sessionToken;
        this.created = created;
        this.expires = expires;
    }

    public static Session createNew(String username, String sessionToken, Instant created, Instant expires){
        return new Session(null, username, sessionToken, created, expires);
    }

    public static Session rehydrate(String username, String sessionToken, Instant created, Instant expires){
        return new Session(null, username, sessionToken, created, expires);
    }

    public Session withId(Long id){
        return new Session(id, this.username, this.sessionToken, this.created, this.expires);
    }

    public String getUsername() {
        return username;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public Instant getCreated() {
        return created;
    }

    public Instant getExpires() {
        return expires;
    }
}
