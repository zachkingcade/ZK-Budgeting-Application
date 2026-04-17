package zachkingcade.dev.ledger.adapter.out.persistence.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountTypeEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.JournalEntryEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.JournalLineEntity;

import java.time.LocalDate;
import java.util.List;

public class JournalEntrySpecifications {

    public static Specification<JournalEntryEntity> belongsToUser(Long userId){
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("userId"), userId);
    }

    public static Specification<JournalEntryEntity> dateAfter(LocalDate desiredDateRangeEnd){
        return(root, query, cb) ->
                desiredDateRangeEnd == null? null : cb.greaterThanOrEqualTo(root.get("entryDate"), desiredDateRangeEnd);
    }

    public static Specification<JournalEntryEntity> dateBefore(LocalDate desiredDateRangeStart){
        return(root, query, cb) ->
                desiredDateRangeStart == null? null : cb.lessThanOrEqualTo(root.get("entryDate"), desiredDateRangeStart);
    }

    public static Specification<JournalEntryEntity> descriptionContains(String desiredText){
        return(root, query, cb) ->
                desiredText == null? null : cb.like(root.get("description"), "%" + desiredText + "%");
    }

    public static Specification<JournalEntryEntity> accountIdsWithin(List<Long> accountIds) {
        return (root, query, cb) -> {
            if (accountIds == null || accountIds.isEmpty()) {
                return null;
            }

            query.distinct(true);

            Join<JournalEntryEntity, JournalLineEntity> lines = root.join("journalLines", JoinType.INNER);
            Join<JournalLineEntity, AccountEntity> account = lines.join("account", JoinType.INNER);

            return account.get("id").in(accountIds);
        };
    }

    public static Specification<JournalEntryEntity> accountTypeIdsWithin(List<Long> accountTypeIds) {
        return (root, query, cb) -> {
            if (accountTypeIds == null || accountTypeIds.isEmpty()) {
                return null;
            }

            query.distinct(true);

            Join<JournalEntryEntity, JournalLineEntity> lines = root.join("journalLines", JoinType.INNER);
            Join<JournalLineEntity, AccountEntity> account = lines.join("account", JoinType.INNER);
            Join<AccountEntity, AccountTypeEntity> type = account.join("type", JoinType.INNER);

            return type.get("id").in(accountTypeIds);
        };
    }

    public static Specification<JournalEntryEntity> notesContains(String desiredText){
        return(root, query, cb) ->
                desiredText == null? null : cb.like(root.get("notes"), "%" + desiredText + "%");
    }

    /** Case-insensitive: entry description, entry notes, or any journal line notes. */
    public static Specification<JournalEntryEntity> searchContainsEntryOrLineNotes(String raw){
        return (root, query, cb) -> {
            if (raw == null || raw.isBlank()) {
                return null;
            }
            String pattern = "%" + raw.toLowerCase() + "%";
            query.distinct(true);
            Join<JournalEntryEntity, JournalLineEntity> lines = root.join("journalLines", JoinType.LEFT);
            return cb.or(
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("notes")), pattern),
                    cb.like(cb.lower(lines.get("notes")), pattern)
            );
        };
    }
}
