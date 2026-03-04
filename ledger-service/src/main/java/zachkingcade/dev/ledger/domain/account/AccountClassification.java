package zachkingcade.dev.ledger.domain.account;

import zachkingcade.dev.ledger.domain.exception.DomainException;

public class AccountClassification {
    private final Long id;
    private final String description;
    private final char creditEffect;
    private final char debitEffect;

    private AccountClassification(Long id, String description, char creditEffect, char debitEffect){
        if(id == null){
            throw new DomainException("AccountClassification requires a id");
        }
        if(description == null || description.isEmpty()){
            throw new DomainException("AccountClassification requires a non-null, non-empty description");
        }
        if(creditEffect != '+' && creditEffect != '-'){
            throw new DomainException("AccountClassification requires creditEffect to be either + or -");
        }
        if(debitEffect != '+' && debitEffect != '-'){
            throw new DomainException("AccountClassification requires debitEffect to be either + or -");
        }

        this.id = id;
        this.description = description;
        this.creditEffect = creditEffect;
        this.debitEffect = debitEffect;
    }

    public Long id() {
        return id;
    }

    public String description() {
        return description;
    }

    public char creditEffect() {
        return creditEffect;
    }

    public char debitEffect() {
        return debitEffect;
    }
}
