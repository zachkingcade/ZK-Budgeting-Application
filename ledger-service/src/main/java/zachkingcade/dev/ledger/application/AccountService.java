package zachkingcade.dev.ledger.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.application.commands.account.GetAllAccountCommand;
import zachkingcade.dev.ledger.application.port.in.account.CreateAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.account.GetByIdAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.account.GetAllAccountsUseCase;
import zachkingcade.dev.ledger.application.port.in.account.UpdateAccountUseCase;
import zachkingcade.dev.ledger.application.commands.account.CreateAccountCommand;
import zachkingcade.dev.ledger.application.commands.account.UpdateAccountCommand;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.port.out.account.AccountRepositoryPort;
import zachkingcade.dev.ledger.application.validation.SortDirection;
import zachkingcade.dev.ledger.domain.account.Account;

import java.util.List;

@Service
public class AccountService implements GetAllAccountsUseCase, GetByIdAccountUseCase, CreateAccountUseCase, UpdateAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    public AccountService(AccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAllAccounts(GetAllAccountCommand command){
        try {
            log.debug("Starting Get All Accounts");
            List<Account> results;
            if(command.sort().isPresent()){
                Sort sort = Sort.by(command.sort().get().direction() == SortDirection.ascending? Sort.Direction.ASC : Sort.Direction.DESC, command.sort().get().type().toString());
                results = accountRepository.findAll(sort);
            } else {
                results = accountRepository.findAll();
            }
            log.debug("Ending Get All Accounts results:[{}]", results.size());
            return results;
        } catch (RuntimeException ex) {
            log.error("AccountService.getAllAccounts failed", ex);
            throw ex;
        }
    }

    public Account getAccountById(Long id){
        try {
            log.debug("Starting Get Account by id:[{}]", id);
            Account result = accountRepository.findById(id);
            log.debug("Ending Get Account by id:[{}]", result.id());
            return result;
        } catch (RuntimeException ex) {
            log.error("AccountService.getAccountById failed for id:[{}]", id, ex);
            throw ex;
        }
    }

    @Override
    public Account createAccount(CreateAccountCommand command) {
        try {
            log.debug("Starting Create Account typeId:[{}] description:[{}]", command.typeId(), command.description());
            Account account = Account.createNew(command.typeId(), command.description(), command.notes().orElse(""));
            Account saved = accountRepository.save(account);
            log.debug("Ending Create Account createdId:[{}]", saved.id());
            return saved;
        } catch (RuntimeException ex) {
            log.error("AccountService.createAccount failed for command:[{}]", command, ex);
            throw ex;
        }
    }

    public Account updateAccount(UpdateAccountCommand command){
        try {
            log.debug("Starting Update Account accountId:[{}] descriptionPresent:[{}] notesPresent:[{}] activePresent:[{}]", command.id(), command.description().isPresent(), command.notes().isPresent(), command.active().isPresent());
            Account account = accountRepository.findById(command.id());

            Account newAccount = Account.rehydrate(
                    account.id(),
                    account.typeId(),
                    command.description().orElse(account.description()),
                    command.active().orElse(account.active()),
                    command.notes().orElse(account.notes())
                    );

            // Check for unique description
            if(command.description().isPresent() && accountRepository.existsByDescription(newAccount.description()) ){
                Account existing = accountRepository.findByDescription(newAccount.description());
                if(!existing.id().equals(newAccount.id()) ){
                    log.debug("Update Account unique-description validation failed description:[{}] existingId:[{}] currentId:[{}]", newAccount.description(), existing.id(), newAccount.id());
                    throw new ApplicationException(String.format("An account already exists with the description: [%s]", newAccount.description()));
                }
            }

            Account saved = accountRepository.save(newAccount);
            log.debug("Ending Update Account updatedId:[{}]", saved.id());
            return saved;
        } catch (RuntimeException ex) {
            log.error("AccountService.updateAccount failed for command:[{}]", command, ex);
            throw ex;
        }
    }
}
