package zachkingcade.dev.ledger.application.port.out.accounttype;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountTypeEntity;
import zachkingcade.dev.ledger.domain.account.AccountType;

import java.util.List;

public interface AccountTypeRepositoryPort {

    AccountType findByIdVisibleToUser(Long userId, Long id);

    List<AccountType> findAllVisibleToUser(Long userId);

    List<AccountType> findAllVisibleToUser(Long userId, Sort sort);

    List<AccountType> findAllVisibleToUser(Long userId, Specification<AccountTypeEntity> spec);

    List<AccountType> findAllVisibleToUser(Long userId, Specification<AccountTypeEntity> spec, Sort sort);

    AccountType findByDescription(String description);

    Boolean existsByDescription(String description);

    AccountType save(AccountType accountTypeToSave);
}
