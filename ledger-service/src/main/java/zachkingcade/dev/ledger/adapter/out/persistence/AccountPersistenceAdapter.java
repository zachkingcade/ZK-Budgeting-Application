package zachkingcade.dev.ledger.adapter.out.persistence;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountTypeEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.AccountJpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.AccountTypeJpaRepository;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.exception.NotFoundException;
import zachkingcade.dev.ledger.application.port.out.account.AccountRepositoryPort;
import zachkingcade.dev.ledger.domain.account.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AccountPersistenceAdapter implements AccountRepositoryPort {

    private final AccountJpaRepository accountJpaRepository;

    private final AccountTypeJpaRepository accountTypeRepository;

    public AccountPersistenceAdapter(AccountJpaRepository accountJpaRepository, AccountTypeJpaRepository accountTypeRepository) {
        this.accountJpaRepository = accountJpaRepository;
        this.accountTypeRepository = accountTypeRepository;
    }

    @Override
    public List<Account> findAll() {
        List<AccountEntity> accountEntitiesList = accountJpaRepository.findAll();
        return convertListOfAccountToDomain(accountEntitiesList);
    }

    @Override
    public List<Account> findAll(Sort sort) {
        List<AccountEntity> accountEntitiesList = accountJpaRepository.findAll(sort);
        return convertListOfAccountToDomain(accountEntitiesList);
    }

    @Override
    public List<Account> findAll(Specification<AccountEntity> spec) {
        List<AccountEntity> accountEntitiesList = accountJpaRepository.findAll(spec);
        return convertListOfAccountToDomain(accountEntitiesList);
    }

    @Override
    public List<Account> findAll(Specification<AccountEntity> spec, Sort sort) {
        List<AccountEntity> accountEntitiesList = accountJpaRepository.findAll(spec,sort);
        return convertListOfAccountToDomain(accountEntitiesList);
    }

    private List<Account> convertListOfAccountToDomain(List<AccountEntity> accountEntityList){
        List<Account> results = new ArrayList<>();
        for(AccountEntity entity : accountEntityList){
            Account newResultItem = Account.rehydrate(entity.getId(), entity.getType().getId(), entity.getDescription(),entity.isActive(), entity.getNotes());
            results.add(newResultItem);
        }
        return results;
    }

    @Override
    public Account findById(Long id) {
        Optional<AccountEntity> entity = accountJpaRepository.findById(id);
        if(entity.isPresent()){
            return Account.rehydrate(entity.get().getId(), entity.get().getType().getId(),entity.get().getDescription(),entity.get().isActive(),entity.get().getNotes());
        } else {
            throw new NotFoundException(String.format("Account not found for id [%s]", id));
        }
    }

    @Override
    public Account findByDescription(String description){
        Optional<AccountEntity> entity  = accountJpaRepository.findByDescription(description);
        if(entity.isPresent()){
            return Account.rehydrate(entity.get().getId(), entity.get().getType().getId(),entity.get().getDescription(),entity.get().isActive(),entity.get().getNotes());
        } else {
            throw new NotFoundException(String.format("Account not found for description [%s]", description));
        }
    }

    @Override
    public Boolean existsByDescription(String description){
        return accountJpaRepository.existsByDescription(description);
    }

    @Override
    public Account save(Account accountToSave) {
        AccountTypeEntity type = accountTypeRepository.findById(accountToSave.typeId()).orElseThrow(
                () -> new ApplicationException(String.format("Unknown typeID: [%s]", accountToSave.typeId())));

        AccountEntity entity = new AccountEntity();
        if(accountToSave.id() != null){
            entity.setId(accountToSave.id());
        }
        entity.setType(type);
        entity.setDescription(accountToSave.description());
        entity.setActive(accountToSave.active());
        entity.setNotes(accountToSave.notes());

        AccountEntity savedEntity = accountJpaRepository.save(entity);

        return accountToSave.withId(savedEntity.getId());
    }
}
