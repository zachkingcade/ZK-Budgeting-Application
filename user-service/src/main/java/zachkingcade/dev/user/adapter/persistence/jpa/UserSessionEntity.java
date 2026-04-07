package zachkingcade.dev.user.adapter.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name="user_sessions")
public class UserSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    @Setter
    @Getter
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Setter
    @Getter
    private UserEntity user;

    @Column(name = "session_token", nullable = false)
    @Setter
    @Getter
    private String sessionToken;

    @Column(name = "created_date", nullable = false)
    @Setter
    @Getter
    private Instant createdDate;

    @Column(name = "expires_date", nullable = false)
    @Setter
    @Getter
    private Instant expiresDate;
}
