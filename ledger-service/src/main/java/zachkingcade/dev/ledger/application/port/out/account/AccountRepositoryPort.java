package zachkingcade.dev.ledger.application.port.out.account;

import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;

import java.util.List;
import java.util.Optional;

public interface AccountRepositoryPort {

    List<AccountEntity> findAll();

    Optional<AccountEntity> findById(Long id);

    AccountEntity findByDescription(String description);

    Boolean existsByDescription(String description);

    AccountEntity save(AccountEntity accountToSave);


}
