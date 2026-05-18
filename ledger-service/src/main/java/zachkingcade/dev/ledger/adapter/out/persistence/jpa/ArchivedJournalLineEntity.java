package zachkingcade.dev.ledger.adapter.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "archived_journal_lines")
@Getter
@Setter
public class ArchivedJournalLineEntity {

    @Id
    @Column(name = "journal_line_id", nullable = false)
    private Long journalLineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private ArchivedJournalEntryEntity journalEntry;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "direction", nullable = false)
    private Character direction;

    @Column(name = "notes", nullable = false)
    private String notes = "";
}
