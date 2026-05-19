package zachkingcade.dev.ledger.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "replayable_journal_entries")
@Getter
@Setter
public class ReplayableJournalEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "replayable_journal_entry_id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "replay_name", nullable = false)
    private String replayName;

    @Column(name = "replay_line_count", nullable = false)
    private int replayLineCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_edited_at", nullable = false)
    private Instant lastEditedAt;

    @OneToMany(mappedBy = "replayableJournalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineOrder ASC")
    private List<ReplayableJournalEntryLineEntity> lines = new ArrayList<>();
}
