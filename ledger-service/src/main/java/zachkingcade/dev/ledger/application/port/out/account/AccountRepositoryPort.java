package zachkingcade.dev.ledger.application.port.out.account;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;
import zachkingcade.dev.ledger.domain.account.Account;

import java.util.List;

public interface AccountRepositoryPort {

    List<Account> findAll();

    List<Account> findAll(Sort sort);

    List<Account> findAll(Specification<AccountEntity> spec);

    List<Account> findAll(Specification<AccountEntity> spec, Sort sort);

    Account findById(Long id);

    Account findByDescription(String description);

    Boolean existsByDescription(String description);

    Account save(Account accountToSave);
}
