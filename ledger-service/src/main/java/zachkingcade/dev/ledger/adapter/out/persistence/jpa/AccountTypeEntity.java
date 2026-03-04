package zachkingcade.dev.ledger.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "account_types")
public class AccountTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    @Setter
    @Getter
    private Long id;

    @Column(name = "type_description", nullable = false, unique = true)
    @Setter
    @Getter
    private String description;

    @Column(name = "notes", nullable = false)
    @Setter
    @Getter
    private String notes = "";

    @Column(name = "type_active", nullable = false)
    @Setter
    @Getter
    private boolean active = true;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "classification_id", nullable = false)
    @Setter
    @Getter
    private AccountClassificationEntity classification;

}
