package zachkingcade.dev.ledger.adapter.out.persistence;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.AccountJpaRepository;
import zachkingcade.dev.ledger.application.port.out.account.AccountRepositoryPort;

import java.util.List;
import java.util.Optional;

@Service
public class AccountPersistenceAdapter implements AccountRepositoryPort {

    private final AccountJpaRepository accountJpaRepository;

    public AccountPersistenceAdapter(AccountJpaRepository accountJpaRepository) {
        this.accountJpaRepository = accountJpaRepository;
    }

    @Override
    public List<AccountEntity> findAll() {
        return accountJpaRepository.findAll();
    }

    @Override
    public Optional<AccountEntity> findById(Long id) {
        return accountJpaRepository.findById(id);
    }

    @Override
    public AccountEntity findByDescription(String description){
        return accountJpaRepository.findByDescription(description);
    }

    @Override
    public Boolean existsByDescription(String description){
        return accountJpaRepository.existsByDescription(description);
    }

    @Override
    public AccountEntity save(AccountEntity accountToSave) {
        return accountJpaRepository.save(accountToSave);
    }
}
