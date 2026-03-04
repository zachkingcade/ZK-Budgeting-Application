package zachkingcade.dev.ledger.domain.journal;

import zachkingcade.dev.ledger.domain.exception.DomainException;

import java.sql.Date;

public class JournalLine {
    private final Long id;
    private final Long journalEntryId;
    private final Long amount;
    private final Long accountId;
    private final char direction;
    private final String notes;

    public JournalLine(Long id, Long journalEntryId, Long amount, Long accountId, char direction, String notes) {
        if(journalEntryId == null){
            throw new DomainException("JournalLine requires a journalEntryId");
        }
        if(amount < 1){
            throw new DomainException("JournalLine requires a positive non zero amount");
        }
        if(accountId == null){
            throw new DomainException("JournalLine requires a accountId");
        }
        if(direction != 'C' && direction != 'D'){
            throw new DomainException("JournalLine requires a direction of either C or D");
        }

        this.id = id;
        this.journalEntryId = journalEntryId;
        this.amount = amount;
        this.accountId = accountId;
        this.direction = direction;
        this.notes = notes;
    }

    public static JournalLine createNew(Long journalEntryId, Long amount, Long accountId, char direction, String notes){
        return new JournalLine(null, journalEntryId, amount, accountId, direction, notes);
    }

    public static JournalLine rehydrate(Long id, Long journalEntryId, Long amount, Long accountId, char direction, String notes) {
        if (id == null) throw new DomainException("id is required for rehydration");
        return new JournalLine(id, journalEntryId, amount, accountId, direction, notes);
    }

    public JournalLine withId(Long id){
        return new JournalLine(id, this.journalEntryId, this.amount, this.accountId, this.direction, this.notes);
    }

    public Long id() {
        return id;
    }

    public Long journalEntryId() {
        return journalEntryId;
    }

    public Long amount() {
        return amount;
    }

    public Long accountId() {
        return accountId;
    }

    public char direction() {
        return direction;
    }

    public String notes() {
        return notes;
    }
}
