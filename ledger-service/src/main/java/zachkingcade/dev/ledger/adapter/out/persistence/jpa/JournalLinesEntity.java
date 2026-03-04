package zachkingcade.dev.ledger.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

public class JournalLinesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "journal_line_id", nullable = false)
    @Getter
    @Setter
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    @Getter
    @Setter
    private JournalEntriesEntity journalEntry;

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
