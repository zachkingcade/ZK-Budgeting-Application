package zachkingcade.dev.ledger.adapter.out.persistence;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountClassificationEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountTypeEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.AccountClassificationJpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.AccountTypeJpaRepository;
import zachkingcade.dev.ledger.application.exception.AccountTypeException;
import zachkingcade.dev.ledger.application.port.out.type.AccountTypeRepositoryPort;
import zachkingcade.dev.ledger.domain.account.AccountType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AccountTypePersistenceAdapter implements AccountTypeRepositoryPort {

    private final AccountTypeJpaRepository accountTypeJpaRepository;

    private final AccountClassificationJpaRepository accountClassificationJpaRepository;

    public AccountTypePersistenceAdapter(AccountTypeJpaRepository accountTypeJpaRepository, AccountClassificationJpaRepository accountClassificationJpaRepository) {
        this.accountTypeJpaRepository = accountTypeJpaRepository;
        this.accountClassificationJpaRepository = accountClassificationJpaRepository;
    }

    @Override
    public List<AccountType> findAll() {
        List<AccountTypeEntity> AccountTypeList = accountTypeJpaRepository.findAll();

        List<AccountType> resultingList = new ArrayList<>();
        for(AccountTypeEntity entity: AccountTypeList){
            resultingList.add(AccountType.rehydrate(entity.getId(), entity.getDescription(),entity.getClassification().getId(),entity.getNotes(),entity.isActive()));
        }

        return resultingList;
    }

    @Override
    public AccountType findById(Long id) {
        Optional<AccountTypeEntity> entity = accountTypeJpaRepository.findById(id);

        if(entity.isPresent()){
            return AccountType.rehydrate(entity.get().getId(), entity.get().getDescription(), entity.get().getClassification().getId(), entity.get().getNotes(), entity.get().isActive());
        } else {
            throw new RuntimeException(String.format("Error: Account Type not found for Account Type id [%s]", id));
        }
    }

    @Override
    public AccountType findByDescription(String description) {
        Optional<AccountTypeEntity> entity = accountTypeJpaRepository.findByDescription(description);

        if(entity.isPresent()){
            return AccountType.rehydrate(entity.get().getId(), entity.get().getDescription(), entity.get().getClassification().getId(), entity.get().getNotes(), entity.get().isActive());
        } else {
            throw new RuntimeException(String.format("Error: Account Type not found for Account description [%s]", description));
        }
    }

    @Override
    public Boolean existsByDescription(String description) {
        return accountTypeJpaRepository.existsByDescription(description);
    }

    @Override
    public AccountType save(AccountType accountTypeToSave) {
        AccountClassificationEntity accountClassification = accountClassificationJpaRepository.findById(accountTypeToSave.classificationId()).orElseThrow( () ->
                new AccountTypeException(String.format("Unable to find Account Type Classificaiton [%s]",accountTypeToSave.classificationId()))
        );

        AccountTypeEntity entity = new AccountTypeEntity();
        if(accountTypeToSave.id() != null){
            entity.setId(accountTypeToSave.id());
        }
        entity.setClassification(accountClassification);
        entity.setDescription(accountTypeToSave.description());
        entity.setActive(accountTypeToSave.active());
        entity.setNotes(accountTypeToSave.notes());

        AccountTypeEntity savedEntity = accountTypeJpaRepository.save(entity);

        return accountTypeToSave.withId(savedEntity.getId());
    }
}
