package zachkingcade.dev.user.adapter.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "service_permissions")
public class ServicePermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Getter
    @Setter
    private Long id;

    @Column(name = "service_name", nullable = false, unique = true)
    @Getter
    @Setter
    private String serviceName;

    @Column(name = "secret_hash", nullable = false)
    @Getter
    @Setter
    private String secretHash;

    @Column(name = "allowed_audiences", nullable = false, length = 2048)
    @Getter
    @Setter
    private String allowedAudiences;

    @Column(name = "allowed_scopes", nullable = false, length = 4096)
    @Getter
    @Setter
    private String allowedScopes;

    @Column(name = "may_act_for_user", nullable = false)
    @Getter
    @Setter
    private boolean mayActForUser;

    @Column(name = "created_date", nullable = false)
    @Getter
    @Setter
    private Instant createdDate;
}
