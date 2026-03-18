package zachkingcade.dev.ledger.domain.journal;

import zachkingcade.dev.ledger.domain.exception.DomainException;

import java.time.LocalDate;
import java.util.List;

public class JournalEntry {
    private final Long id;
    private final LocalDate entryDate;
    private final String description;
    private final String notes;

    private final List<JournalLine> journalLines;

    private JournalEntry(Long id, LocalDate entryDate, String description, String notes, List<JournalLine> journalLines) {
        if(entryDate == null){
            throw new DomainException("JournalEntry requires a entryDate");
        }
        if(description == null || description.isEmpty()){
            throw new DomainException("JournalEntry requires a non-null, non-empty description");
        }
        if(journalLines.size() < 2){
            throw new DomainException("JournalEntry requires at least two Journal Lines");
        }
        Long credit = 0L;
        Long debit = 0L;
        for (JournalLine line : journalLines){
            if(line.direction() == 'C'){
                credit += line.amount();
            }
            if(line.direction() == 'D'){
                debit += line.amount();
            }
        }
        if(!credit.equals(debit)){
            throw new DomainException("JournalEntry requires credit and debit sums match");
        }
        
        this.id = id;
        this.entryDate = entryDate;
        this.description = description;
        this.notes = notes;
        this.journalLines = journalLines;
    }

    public static JournalEntry createNew(LocalDate entryDate, String description, String notes, List<JournalLine> journalLines){
        return new JournalEntry(null, entryDate, description,  notes, journalLines);
    }

    public static JournalEntry rehydrate(Long id,LocalDate entryDate, String description, String notes, List<JournalLine> journalLines) {
        if (id == null) throw new DomainException("id is required for rehydration");
        return new JournalEntry(id, entryDate, description, notes, journalLines);
    }

    public JournalEntry withId(Long id){
        return new JournalEntry(id, this.entryDate, this.description, this.notes, this.journalLines);
    }

    public Long id() {
        return id;
    }

    public LocalDate entryDate() {
        return entryDate;
    }

    public String description() {
        return description;
    }

    public String notes() {
        return notes;
    }

    public List<JournalLine> journalLines() {
        return journalLines;
    }
}
