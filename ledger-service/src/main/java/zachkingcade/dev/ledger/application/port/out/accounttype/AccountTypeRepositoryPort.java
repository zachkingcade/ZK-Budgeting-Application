package zachkingcade.dev.ledger.application.port.out.accounttype;

import org.springframework.data.domain.Sort;
import zachkingcade.dev.ledger.domain.account.AccountType;

import java.util.List;

public interface AccountTypeRepositoryPort {

    public AccountType findById(Long id);

    List<AccountType> findAll();

    List<AccountType> findAll(Sort sort);

    AccountType findByDescription(String description);

    Boolean existsByDescription(String description);

    AccountType save(AccountType accountTypeToSave);

}
