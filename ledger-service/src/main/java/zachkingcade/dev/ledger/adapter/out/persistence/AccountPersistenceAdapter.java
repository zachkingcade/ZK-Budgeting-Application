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
    public List<Account> findAll(Long userId) {
        List<AccountEntity> accountEntitiesList = accountJpaRepository.findAll((root, query, cb) -> cb.equal(root.get("userId"), userId));
        return convertListOfAccountToDomain(accountEntitiesList);
    }

    @Override
    public List<Account> findAll(Long userId, Sort sort) {
        List<AccountEntity> accountEntitiesList = accountJpaRepository.findAll((root, query, cb) -> cb.equal(root.get("userId"), userId), sort);
        return convertListOfAccountToDomain(accountEntitiesList);
    }

    @Override
    public List<Account> findAll(Long userId, Specification<AccountEntity> spec) {
        Specification<AccountEntity> scopedSpec = ((Specification<AccountEntity>) (root, query, cb) -> cb.equal(root.get("userId"), userId)).and(spec);
        List<AccountEntity> accountEntitiesList = accountJpaRepository.findAll(scopedSpec);
        return convertListOfAccountToDomain(accountEntitiesList);
    }

    @Override
    public List<Account> findAll(Long userId, Specification<AccountEntity> spec, Sort sort) {
        Specification<AccountEntity> scopedSpec = ((Specification<AccountEntity>) (root, query, cb) -> cb.equal(root.get("userId"), userId)).and(spec);
        List<AccountEntity> accountEntitiesList = accountJpaRepository.findAll(scopedSpec,sort);
        return convertListOfAccountToDomain(accountEntitiesList);
    }

    private List<Account> convertListOfAccountToDomain(List<AccountEntity> accountEntityList){
        List<Account> results = new ArrayList<>();
        for(AccountEntity entity : accountEntityList){
            Account newResultItem = Account.rehydrate(entity.getId(), entity.getType().getId(), entity.getDescription(),entity.isActive(), entity.getNotes(), entity.getUserId());
            results.add(newResultItem);
        }
        return results;
    }

    @Override
    public Account findById(Long userId, Long id) {
        Optional<AccountEntity> entity = accountJpaRepository.findByIdAndUserId(id, userId);
        if(entity.isPresent()){
            return Account.rehydrate(entity.get().getId(), entity.get().getType().getId(),entity.get().getDescription(),entity.get().isActive(),entity.get().getNotes(), entity.get().getUserId());
        } else {
            throw new NotFoundException(String.format("Account not found for id [%s]", id));
        }
    }

    @Override
    public Account findByDescription(Long userId, String description){
        Optional<AccountEntity> entity  = accountJpaRepository.findByDescriptionAndUserId(description, userId);
        if(entity.isPresent()){
            return Account.rehydrate(entity.get().getId(), entity.get().getType().getId(),entity.get().getDescription(),entity.get().isActive(),entity.get().getNotes(), entity.get().getUserId());
        } else {
            throw new NotFoundException(String.format("Account not found for description [%s]", description));
        }
    }

    @Override
    public Boolean existsByDescription(Long userId, String description){
        return accountJpaRepository.existsByDescriptionAndUserId(description, userId);
    }

    @Override
    public Account save(Account accountToSave) {
        AccountTypeEntity type = accountTypeRepository.findByIdAndUserId(accountToSave.typeId(), accountToSave.getUserId())
                .or(() -> accountTypeRepository.findByIdAndSystemAccountTrue(accountToSave.typeId()))
                .orElseThrow(() -> new ApplicationException(String.format("Unknown or unauthorized typeID: [%s]", accountToSave.typeId())));

        AccountEntity entity = new AccountEntity();
        if(accountToSave.id() != null){
            entity.setId(accountToSave.id());
        }
        entity.setType(type);
        entity.setDescription(accountToSave.description());
        entity.setActive(accountToSave.active());
        entity.setNotes(accountToSave.notes());
        entity.setUserId(accountToSave.getUserId());

        AccountEntity savedEntity = accountJpaRepository.save(entity);

        return accountToSave.withId(savedEntity.getId());
    }
}
