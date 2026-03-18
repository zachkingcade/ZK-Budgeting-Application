package zachkingcade.dev.ledger.application.port.out.accounttype;

import zachkingcade.dev.ledger.domain.account.AccountType;

import java.util.List;

public interface AccountTypeRepositoryPort {

    public AccountType findById(Long id);

    List<AccountType> findAll();

    AccountType findByDescription(String description);

    Boolean existsByDescription(String description);

    AccountType save(AccountType accountTypeToSave);
}
