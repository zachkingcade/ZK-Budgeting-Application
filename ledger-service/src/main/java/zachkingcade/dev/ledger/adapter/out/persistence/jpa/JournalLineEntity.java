package zachkingcade.dev.ledger.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "journal_lines")
public class JournalLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "journal_line_id", nullable = false)
    @Getter
    @Setter
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    @Getter
    @Setter
    private JournalEntryEntity journalEntry;

    @Column(name = "amount", nullable = false)
    @Getter
    @Setter
    private Long amount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @Getter
    @Setter
    private AccountEntity account;

    @Column(name = "direction", nullable = false)
    @Getter
    @Setter
    private char direction;

    @Column(name = "notes", nullable = false)
    @Getter
    @Setter
    private String notes;
}
