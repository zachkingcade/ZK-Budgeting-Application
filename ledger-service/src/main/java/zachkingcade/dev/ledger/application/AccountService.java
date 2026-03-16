package zachkingcade.dev.ledger.application;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountTypeEntity;
import zachkingcade.dev.ledger.application.port.in.account.CreateAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.account.GetByIdAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.account.GetallAccountsUseCase;
import zachkingcade.dev.ledger.application.port.in.account.UpdateAccountUseCase;
import zachkingcade.dev.ledger.application.commands.account.CreateAccountCommand;
import zachkingcade.dev.ledger.application.commands.account.GetByIdAccountCommand;
import zachkingcade.dev.ledger.application.commands.account.UpdateAccountCommand;
import zachkingcade.dev.ledger.application.exception.AccountException;
import zachkingcade.dev.ledger.application.port.out.account.AccountRepositoryPort;
import zachkingcade.dev.ledger.application.port.out.type.AccountTypeRepositoryPort;
import zachkingcade.dev.ledger.domain.account.Account;

import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService implements GetallAccountsUseCase, GetByIdAccountUseCase, CreateAccountUseCase, UpdateAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountTypeRepositoryPort accountTypeRepository;

    public AccountService(AccountRepositoryPort accountRepository, AccountTypeRepositoryPort accountTypeRepository) {
        this.accountRepository = accountRepository;
        this.accountTypeRepository = accountTypeRepository;
    }

    public List<Account> getAllAccounts(){
        // Get entities
        List<AccountEntity> accountEntitiesList = accountRepository.findAll();

        // Convert to domain objects
        List<Account> results = new ArrayList<Account>();
        for(AccountEntity entity : accountEntitiesList){
            Account newResultItem = Account.rehydrate(entity.getId(), entity.getType().getId(), entity.getDescription(),entity.isActive(), entity.getNotes());
            results.add(newResultItem);
        }

        return results;
    };

    public Account getAccountById(GetByIdAccountCommand command){
        // Get entity
        AccountEntity entity = accountRepository.findById(command.id()).orElseThrow(() -> new AccountException(String.format("Unknown account iD: [%s]", command.id())));

        // Convert to domain object
        return Account.rehydrate(entity.getId(), entity.getType().getId(), entity.getDescription(),entity.isActive(), entity.getNotes());
    };

    @Override
    public Account createAccount(CreateAccountCommand command) throws AccountException {
        AccountTypeEntity type = accountTypeRepository.findById(command.typeId()).orElseThrow(() -> new AccountException(String.format("Unknown typeID: [%s]", command.typeId())));

        // Create new account
        Account account = Account.createNew(command.typeId(), command.description(), command.notes().orElse(""));

        //Create new account entity and persist
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setType(type);
        accountEntity.setDescription(command.description());
        accountEntity.setNotes(command.notes().orElse(""));
        AccountEntity saved = accountRepository.save(accountEntity);

        //return account with newly persisted id
        return account.withId(saved.getId());
    }

    public Account updateAccount(UpdateAccountCommand command){
        // Get entity
        AccountEntity entity = accountRepository.findById(command.id()).orElseThrow(() -> new AccountException(String.format("Unknown account iD: [%s]", command.id())));

        // Update present modifications
        command.description().ifPresent(entity::setDescription);
        command.notes().ifPresent(entity::setNotes);
        command.active().ifPresent(entity::setActive);

        // Check for unique description
        if(command.description().isPresent() && accountRepository.existsByDescription(entity.getDescription()) && !accountRepository.findByDescription(entity.getDescription()).getId().equals(entity.getId()) ){
            throw new AccountException(String.format("An account already exists with the description: [%s]", entity.getDescription()));
        }

        // Create new Domain Account
        Account updatedAccount = Account.createNew(entity.getId(), entity.getDescription(), entity.getNotes());

        //persist and return
        AccountEntity saved = accountRepository.save(entity);
        return updatedAccount.withId(saved.getId());
    };
}
