package zachkingcade.dev.ledger.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id", nullable = false)
    @Setter
    @Getter
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_type_id", nullable = false)
    @Setter
    @Getter
    private AccountTypeEntity type;

    @Column(name = "account_description", nullable = false)
    @Setter
    @Getter
    private String description;

    @Column(name = "account_active", nullable = false)
    @Setter
    @Getter
    private boolean active = true;

    @Column(name = "notes", nullable = false)
    @Setter
    @Getter
    private String notes = "";

    @Column(name = "user_id", nullable = false)
    @Setter
    @Getter
    private Long userId;
}
