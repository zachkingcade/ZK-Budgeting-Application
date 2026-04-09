package zachkingcade.dev.ledger.application.port.out.account;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;
import zachkingcade.dev.ledger.domain.account.Account;

import java.util.List;

public interface AccountRepositoryPort {

    List<Account> findAll(Long userId);

    List<Account> findAll(Long userId, Sort sort);

    List<Account> findAll(Long userId, Specification<AccountEntity> spec);

    List<Account> findAll(Long userId, Specification<AccountEntity> spec, Sort sort);

    Account findById(Long userId, Long id);

    Account findByDescription(Long userId, String description);

    Boolean existsByDescription(Long userId, String description);

    Account save(Account accountToSave);
}
