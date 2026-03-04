package zachkingcade.dev.ledger.domain.journal;

import zachkingcade.dev.ledger.domain.exception.DomainException;

import java.sql.Date;

public class JournalEntry {
    private final Long id;
    private final Date entryDate;
    private final String description;
    private final String notes;

    private JournalEntry(Long id, Date entryDate, String description, String notes) {
        if(entryDate == null){
            throw new DomainException("JournalEntry requires a entryDate");
        }
        if(description == null || description.isEmpty()){
            throw new DomainException("JournalEntry requires a non-null, non-empty description");
        }
        
        this.id = id;
        this.entryDate = entryDate;
        this.description = description;
        this.notes = notes;
    }

    public static JournalEntry createNew(Date entryDate, String description, String notes){
        return new JournalEntry(null, entryDate, description,  notes);
    }

    public static JournalEntry rehydrate(Long id,Date entryDate, String description, String notes) {
        if (id == null) throw new DomainException("id is required for rehydration");
        return new JournalEntry(id, entryDate, description, notes);
    }

    public JournalEntry withId(Long id){
        return new JournalEntry(id, this.entryDate, this.description, this.notes);
    }

    public Long id() {
        return id;
    }

    public Date entryDate() {
        return entryDate;
    }

    public String description() {
        return description;
    }

    public String notes() {
        return notes;
    }
}
