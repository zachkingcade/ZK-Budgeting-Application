package zachkingcade.dev.user.adapter.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    @Setter
    @Getter
    private Long userId;

    @Column(name = "username", nullable = false)
    @Setter
    @Getter
    private String username;

    @Column(name = "password_hash", nullable = false)
    @Setter
    @Getter
    private String passwordHash;

    @Column(name = "active", nullable = false)
    @Setter
    @Getter
    private Boolean active = true;

    @Column(name = "created_date", nullable = false)
    @Setter
    @Getter
    private Instant createdDate;

    @Column(name = "updated_date")
    @Setter
    @Getter
    private Instant updatedDate;
}
