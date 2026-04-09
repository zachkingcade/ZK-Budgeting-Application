package zachkingcade.dev.ledger.domain.account;

import zachkingcade.dev.ledger.domain.exception.DomainException;

public class AccountType {
    private final Long id;
    private final String description;
    private final String notes;
    private final boolean active;
    private final Long classificationId;
    private final Long userId;
    private final boolean systemAccount;

    private AccountType(Long id, String description, Long classificationId, String notes, boolean active, Long userId, boolean systemAccount) {
        if(description == null || description.isEmpty()){
            throw new DomainException("AccountType requires a non-null, non-empty description");
        }
        if(classificationId == null){
            throw new DomainException("AccountType requires a classification");
        }
        if(systemAccount && userId != -1){
            throw new DomainException("AccountType requires system accounts have a user id of -1");
        }
        if(!systemAccount && (userId == null || userId < 0)){
            throw new DomainException("Account types require a non negative user id number for non system account types.");
        }

        this.id = id;
        this.description = description;
        this.notes = notes;
        this.active = active;
        this.classificationId = classificationId;
        this.userId = userId;
        this.systemAccount = systemAccount;
    }

    public static AccountType createNew(String description, Long classificationId, String notes, Long userId, boolean systemAccount){
        return new AccountType(null,description, classificationId, notes, true, userId, systemAccount);
    }

    public static AccountType rehydrate(Long id, String description, Long classificationId, String notes, boolean active, Long userId, boolean systemAccount){
        if (id == null) throw new DomainException("id is required for rehydration");
        return new AccountType(id,description, classificationId, notes, active, userId, systemAccount);
    }

    public AccountType withId(Long id){
        return new AccountType(id, this.description, this.classificationId, this.notes, this.active, this.userId, this.systemAccount);
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

    public Long getUserId() {return userId;}

    public Boolean getSystemAccount() {return systemAccount;}
}
