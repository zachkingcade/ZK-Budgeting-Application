package zachkingcade.dev.ledger.application;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.out.persistence.AccountJpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.AccountTypeJpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountTypeEntity;
import zachkingcade.dev.ledger.application.abstracts.CreateAccountUseCase;
import zachkingcade.dev.ledger.application.commands.CreateAccountCommand;
import zachkingcade.dev.ledger.application.exception.AccountException;
import zachkingcade.dev.ledger.domain.account.Account;

@Service
public class AccountService implements CreateAccountUseCase {
    private final AccountJpaRepository accountRepo;
    private final AccountTypeJpaRepository typeRepo;

    public AccountService(AccountJpaRepository accountRepo, AccountTypeJpaRepository typeRepo) {
        this.accountRepo = accountRepo;
        this.typeRepo = typeRepo;
    }

    @Override
    public Account createAccount(CreateAccountCommand command) throws AccountException {
        AccountTypeEntity type = typeRepo.findById(command.typeId()).orElseThrow(() -> new AccountException(String.format("Unknown typeID: [%s]", command.typeId())));

        // Create new account
        Account account = Account.createNew(command.typeId(), command.description(), command.notes().orElse(""));

        //Create new account entity and persist
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setType(type);
        accountEntity.setDescription(command.description());
        accountEntity.setNotes(command.notes().orElse(""));
        AccountEntity saved = accountRepo.save(accountEntity);

        //return account with newly persisted id
        return account.withId(saved.getId());
    }
}
