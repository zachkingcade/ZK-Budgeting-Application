package zachkingcade.dev.ledger.adapter.out.persistence.specification;

import org.springframework.data.jpa.domain.Specification;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountTypeEntity;
import zachkingcade.dev.ledger.domain.account.AccountType;

import java.util.List;

public class AccountTypeSpecifications {

    public static Specification<AccountTypeEntity> visibleToUser(Long userId){
        return (root, query, cb) -> {
            if (userId == null) return null;
            return cb.or(
                    cb.equal(root.get("userId"), userId),
                    cb.isTrue(root.get("systemAccount"))
            );
        };
    }

    public static Specification<AccountTypeEntity> descriptionContains(String desiredText){
        return(root, query, cb) ->
                desiredText == null? null : cb.like(root.get("description"), "%" + desiredText + "%");
    }

    public static Specification<AccountTypeEntity> classIdWithin(List<Long> desiredIdsList){
        return(root, query, cb) -> {
            if (desiredIdsList == null || desiredIdsList.isEmpty()) {
                return null;
            }
            return root.get("classification").get("id").in(desiredIdsList);
        };
    }

    public static Specification<AccountTypeEntity> notesContains(String desiredText){
        return(root, query, cb) ->
                desiredText == null? null : cb.like(root.get("notes"), "%" + desiredText + "%");
    }

    public static Specification<AccountTypeEntity> hideInactive(Boolean use){
        return(root, query, cb) ->{
            if(use != null && use){
                return cb.equal(root.get("active"),true);
            }
            return null;
        };
    }

    public static Specification<AccountTypeEntity> hideActive(Boolean use){
        return(root, query, cb) ->{
            if(use != null && use){
                return cb.equal(root.get("active"),false);
            }
            return null;
        };
    }
}
