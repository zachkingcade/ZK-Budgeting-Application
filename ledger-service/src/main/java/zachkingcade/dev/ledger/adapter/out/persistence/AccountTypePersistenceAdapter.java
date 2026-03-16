package zachkingcade.dev.ledger.adapter.out.persistence;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountTypeEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.AccountTypeJpaRepository;
import zachkingcade.dev.ledger.application.port.out.type.AccountTypeRepositoryPort;

import java.util.Optional;

@Service
public class AccountTypePersistenceAdapter implements AccountTypeRepositoryPort {

    private final AccountTypeJpaRepository accountTypeJpaRepository;

    public AccountTypePersistenceAdapter(AccountTypeJpaRepository accountTypeJpaRepository) {
        this.accountTypeJpaRepository = accountTypeJpaRepository;
    }

    @Override
    public Optional<AccountTypeEntity> findById(Long id) {
        return accountTypeJpaRepository.findById(id);
    }
}
