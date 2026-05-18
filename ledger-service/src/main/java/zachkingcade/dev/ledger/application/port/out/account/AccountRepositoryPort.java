package zachkingcade.dev.ledger.application.port.out.account;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;
import zachkingcade.dev.ledger.domain.account.Account;

import java.util.Collection;
import java.util.List;

public interface AccountRepositoryPort {

    List<Account> findAll(Long userId);

    List<Account> findAll(Long userId, Sort sort);

    List<Account> findAll(Long userId, Specification<AccountEntity> spec);

    List<Account> findAll(Long userId, Specification<AccountEntity> spec, Sort sort);

    Account findById(Long userId, Long id);

    Account findByDescriptionAndTypeId(Long userId, Long typeId, String description);

    Boolean existsByDescriptionAndTypeId(Long userId, Long typeId, String description);

    Account save(Account accountToSave);

    boolean accountHasClassification(Long userId, Long accountId, String classificationDescription);

    boolean accountHasTypeDescription(Long userId, Long accountId, String typeDescription);

    /** True when the account's type description is one of {@code typeDescriptions} (e.g. budget-pool asset types). */
    boolean accountHasAnyOfTypeDescriptions(Long userId, Long accountId, Collection<String> typeDescriptions);
}
