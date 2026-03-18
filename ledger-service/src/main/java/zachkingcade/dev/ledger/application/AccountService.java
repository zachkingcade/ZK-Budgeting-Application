package zachkingcade.dev.ledger.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.application.port.in.account.CreateAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.account.GetByIdAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.account.GetAllAccountsUseCase;
import zachkingcade.dev.ledger.application.port.in.account.UpdateAccountUseCase;
import zachkingcade.dev.ledger.application.commands.account.CreateAccountCommand;
import zachkingcade.dev.ledger.application.commands.account.UpdateAccountCommand;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.port.out.account.AccountRepositoryPort;
import zachkingcade.dev.ledger.domain.account.Account;

import java.util.List;

@Service
public class AccountService implements GetAllAccountsUseCase, GetByIdAccountUseCase, CreateAccountUseCase, UpdateAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    public AccountService(AccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAllAccounts(){
        return accountRepository.findAll();
    }

    public Account getAccountById(Long id){
        return accountRepository.findById(id);
    }

    @Override
    public Account createAccount(CreateAccountCommand command) {
        log.info("Starting Create Account for new account [{}]", command.description());
        Account account = Account.createNew(command.typeId(), command.description(), command.notes().orElse(""));
        return accountRepository.save(account);
    }

    public Account updateAccount(UpdateAccountCommand command){
        log.info("Starting Update Account for account [{}][{}]", command.id(),command.description());
        Account account = accountRepository.findById(command.id());

        Account newAccount = Account.rehydrate(
                account.id(),
                account.typeId(),
                command.description().orElse(account.description()),
                command.active().orElse(account.active()),
                command.notes().orElse(account.notes())
                );

        // Check for unique description
        if(command.description().isPresent() && accountRepository.existsByDescription(newAccount.description()) && !accountRepository.findByDescription(newAccount.description()).id().equals(newAccount.id()) ){
            throw new ApplicationException(String.format("An account already exists with the description: [%s]", newAccount.description()));
        }

        return accountRepository.save(newAccount);
    }
}
