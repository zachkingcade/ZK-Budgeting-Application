package zachkingcade.dev.ledger.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "account_classifications")
public class AccountClassificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "classifications_id", nullable = false)
    @Getter
    @Setter
    private Long id;

    @Column(name = "classification_description", nullable = false)
    @Getter
    @Setter
    private String description;

    @Column(name = "credit_effect", nullable = false)
    @Getter
    @Setter
    private char creditEffect;

    @Column(name = "debit_effect", nullable = false)
    @Getter
    @Setter
    private char debitEffect;
}
