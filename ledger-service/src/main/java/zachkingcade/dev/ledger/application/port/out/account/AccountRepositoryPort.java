package zachkingcade.dev.ledger.application.port.out.account;

import zachkingcade.dev.ledger.domain.account.Account;

import java.util.List;

public interface AccountRepositoryPort {

    List<Account> findAll();

    Account findById(Long id);

    Account findByDescription(String description);

    Boolean existsByDescription(String description);

    Account save(Account accountToSave);


}
