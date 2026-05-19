package zachkingcade.dev.ledger.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "replayable_journal_entry_lines")
@Getter
@Setter
public class ReplayableJournalEntryLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "replayable_journal_entry_line_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "replayable_journal_entry_id", nullable = false)
    private ReplayableJournalEntryEntity replayableJournalEntry;

    @Column(name = "line_order", nullable = false)
    private int lineOrder;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "direction", nullable = false, length = 1)
    private Character direction;
}
