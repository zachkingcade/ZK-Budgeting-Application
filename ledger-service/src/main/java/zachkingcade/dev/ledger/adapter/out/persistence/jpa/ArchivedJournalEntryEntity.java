package zachkingcade.dev.ledger.adapter.out.persistence.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "archived_journal_entries")
@Getter
@Setter
public class ArchivedJournalEntryEntity {

    @Id
    @Column(name = "journal_entry_id", nullable = false)
    private Long journalEntryId;

    @Column(name = "entry_date", nullable = false)
    private Date entryDate;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "notes", nullable = false)
    private String notes = "";

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "closed_accounting_period_id", nullable = false)
    private Long closedAccountingPeriodId;

    @OneToMany(mappedBy = "journalEntry", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArchivedJournalLineEntity> journalLines = new ArrayList<>();
}
