package zachkingcade.dev.ledger.domain.account;

import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountClassificationEntity;
import zachkingcade.dev.ledger.domain.exception.DomainException;

public class AccountType {
    private final Long id;
    private final String description;
    private final String notes;
    private final boolean active;
    private final Long classificationId;

    private AccountType(Long id, String description, Long classificationId, String notes, boolean active) {
        if(description == null || description.isEmpty()){
            throw new DomainException("AccountType requires a non-null, non-empty description");
        }
        if(classificationId == null){
            throw new DomainException("AccountType requires a classification");
        }
        this.id = id;
        this.description = description;
        this.notes = notes;
        this.active = active;
        this.classificationId = classificationId;
    }

    public static AccountType createNew(String description, Long classificationId, String notes){
        return new AccountType(null,description, classificationId, notes, true);
    }

    public static AccountType rehydrate(Long id, String description, Long classificationId, String notes, boolean active){
        if (id == null) throw new DomainException("id is required for rehydration");
        return new AccountType(id,description, classificationId, notes, active);
    }

    public AccountType withId(Long id){
        return new AccountType(id, this.description, this.classificationId, this.notes, this.active);
    }


    public Long id() {
        return id;
    }

    public String description() {
        return description;
    }

    public String notes() {
        return notes;
    }

    public boolean active() {
        return active;
    }

    public Long classificationId() {
        return classificationId;
    }
}
