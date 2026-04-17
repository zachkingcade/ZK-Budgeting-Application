package zachkingcade.dev.ledger.adapter.out.persistence.specification;

import org.springframework.data.jpa.domain.Specification;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;

import java.util.List;

public class AccountSpecifications {

    public static Specification<AccountEntity> belongsToUser(Long userId){
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("userId"), userId);
    }

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

    /** Case-insensitive match on description or notes. */
    public static Specification<AccountEntity> searchContainsDescriptionOrNotes(String raw){
        return (root, query, cb) -> {
            if (raw == null || raw.isBlank()) {
                return null;
            }
            String pattern = "%" + raw.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("notes")), pattern)
            );
        };
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
