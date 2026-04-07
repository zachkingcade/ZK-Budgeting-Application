package zachkingcade.dev.user.domain.user;

import zachkingcade.dev.user.domain.exception.DomainException;

import java.time.Instant;
import java.util.regex.Pattern;

public class User {
    private final Long user_id;
    private final String username;
    private final String password;
    private final Boolean active;
    private final Instant created_date;
    private final Instant updated_date;

    private User(Long user_id, String username, String password, Boolean active, Instant created_date, Instant updated_date) {
        this.user_id = user_id;
        this.username = username;
        this.password = password;
        this.active = active;
        this.created_date = created_date;
        this.updated_date = updated_date;
    }

    public static User createNew(String username, String password, boolean active, Instant created_date, Instant updated_date){
        // Username Rules
        if(username.length() <= 6){
            throw new DomainException("Username must contain more then 6 characters");
        }
        if(username.contains("@")){
            throw new DomainException("Username must not contain the at (@) special character");
        }

        //Password Rules
        if(password.length() <= 6){
            throw new DomainException("Password must contain more then 6 characters");
        }
        if(!password.matches(".*\\d.*")){
            throw new DomainException("Password must contain at least one number");
        }
        if(!Pattern.compile("[^a-zA-Z0-9]").matcher(password).find()){
            throw new DomainException("Password must must contain at least one special characters");
        }
        if(password.chars().noneMatch(Character::isUpperCase)){
            throw new DomainException("Password must must contain at least one uppercase letter");
        }

        return new User(null, username, password, active, created_date,updated_date);
    }

    public static User rehydrate(Long user_id, String username, String password, Boolean active, Instant created_date, Instant updated_date){
        return new User(user_id, username, password, active, created_date, updated_date);
    }

    public static User rehydrate(Long user_id, String username, Boolean active, Instant created_date, Instant updated_date){
        return new User(user_id, username, null, active, created_date, updated_date);
    }

    public User withId(Long id){
        return new User(id, this.username, this.password, this.active, this.created_date, this.updated_date);
    }

    public Long getUser_id() {
        return user_id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getActive() {
        return active;
    }

    public Instant getCreated_date() {
        return created_date;
    }

    public Instant getUpdated_date() {
        return updated_date;
    }
}


