package zachkingcade.dev.ledger.domain.account;

import zachkingcade.dev.ledger.domain.exception.DomainException;

public class Account {
    private final Long id;
    private final Long typeId;
    private final String description;
    private final boolean active;
    private final String notes;

    private Account(Long id, Long typeId, String description, boolean active, String notes){
        if(typeId == null){
            throw new DomainException("Account requires a typeId");
        }
        if(description == null || description.isEmpty()){
            throw new DomainException("Account requires a non-null, non-empty description");
        }

        this.id = id;
        this.typeId = typeId;
        this.description = description;
        this.active = active;
        this.notes = (notes == null)? "" : notes;
    }
    
    public static Account createNew(Long typeId, String description, String notes){
        return new Account(null, typeId, description, true, notes);
    }

    public static Account rehydrate(Long id, Long typeId, String description, boolean active, String notes) {
        if (id == null) throw new DomainException("id is required for rehydration");
        return new Account(id, typeId, description, active, notes);
    }

    public Account withId(Long id){
        return new Account(id, this.typeId, this.description, this.active, this.notes);
    }

    public Long id() {
        return id;
    }

    public Long typeId() {
        return typeId;
    }

    public String description() {
        return description;
    }

    public boolean active() {
        return active;
    }

    public String notes() {
        return notes;
    }


}
