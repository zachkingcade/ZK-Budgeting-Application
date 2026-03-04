package zachkingcade.dev.ledger.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Entity
@Table(name = "journal_entries")
public class JournalEntriesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "journal_entry_id", nullable = false)
    @Getter
    @Setter
    private Long id;

    @Column(name = "entry_date", nullable = false)
    @Getter
    @Setter
    private Date entryDate;

    @Column(name = "description", nullable = false)
    @Getter
    @Setter
    private String description;

    @Column(name = "notes", nullable = false)
    @Getter
    @Setter
    private String notes = "";
}
