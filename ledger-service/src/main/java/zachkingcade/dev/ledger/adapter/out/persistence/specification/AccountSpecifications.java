package zachkingcade.dev.ledger.adapter.out.persistence.specification;

import org.springframework.data.jpa.domain.Specification;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;

import java.util.List;

public class AccountSpecifications {

    public static Specification<AccountEntity> descriptionContains(String desiredText){
        return(root, query, cb) ->
            desiredText == null? null : cb.like(root.get("description"), "%" + desiredText + "%");
    }

    public static Specification<AccountEntity> typeIdWithin(List<Long> desiredIdsList){
        return(root, query, cb) -> {
            if (desiredIdsList == null || desiredIdsList.isEmpty()) {
                return null;
            }
            return root.get("type").get("id").in(desiredIdsList);
        };
    }

    public static Specification<AccountEntity> notesContains(String desiredText){
        return(root, query, cb) ->
                desiredText == null? null : cb.like(root.get("notes"), "%" + desiredText + "%");
    }

    public static Specification<AccountEntity> hideInactive(Boolean use){
        return(root, query, cb) ->{
            if(use != null && use){
                return cb.equal(root.get("active"),true);
            }
            return null;
        };
    }

    public static Specification<AccountEntity> hideActive(Boolean use){
        return(root, query, cb) ->{
            if(use != null && use){
                return cb.equal(root.get("active"),false);
            }
            return null;
        };
    }
}
