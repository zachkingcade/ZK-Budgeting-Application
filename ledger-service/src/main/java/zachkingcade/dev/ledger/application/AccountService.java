package zachkingcade.dev.ledger.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.specification.AccountSpecifications;
import zachkingcade.dev.ledger.application.commands.account.GetAllAccountCommand;
import zachkingcade.dev.ledger.application.commands.account.GetByIdAccountCommand;
import zachkingcade.dev.ledger.application.port.in.account.CreateAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.account.GetByIdAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.account.GetAllAccountsUseCase;
import zachkingcade.dev.ledger.application.port.in.account.UpdateAccountUseCase;
import zachkingcade.dev.ledger.application.commands.account.CreateAccountCommand;
import zachkingcade.dev.ledger.application.commands.account.UpdateAccountCommand;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.port.in.journal.GetBalanceForAccountUseCase;
import zachkingcade.dev.ledger.application.port.out.account.AccountRepositoryPort;
import zachkingcade.dev.ledger.application.port.out.accounttype.AccountClassificationRepositoryPort;
import zachkingcade.dev.ledger.application.port.out.accounttype.AccountTypeRepositoryPort;
import zachkingcade.dev.ledger.application.validation.SortDirection;
import zachkingcade.dev.ledger.domain.account.Account;
import zachkingcade.dev.ledger.domain.account.AccountClassification;
import zachkingcade.dev.ledger.domain.account.AccountType;

import java.util.List;

import static zachkingcade.dev.ledger.adapter.out.persistence.specification.AccountSpecifications.*;

@Service
public class AccountService implements GetAllAccountsUseCase, GetByIdAccountUseCase, CreateAccountUseCase, UpdateAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final GetBalanceForAccountUseCase getBalanceForAccountUseCase;
    private final AccountTypeRepositoryPort accountTypeRepositoryPort;
    private final AccountClassificationRepositoryPort accountClassificationRepositoryPort;
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    public AccountService(
            AccountRepositoryPort accountRepository,
            GetBalanceForAccountUseCase getBalanceForAccountUseCase,
            AccountTypeRepositoryPort accountTypeRepositoryPort,
            AccountClassificationRepositoryPort accountClassificationRepositoryPort) {
        this.accountRepository = accountRepository;
        this.getBalanceForAccountUseCase = getBalanceForAccountUseCase;
        this.accountTypeRepositoryPort = accountTypeRepositoryPort;
        this.accountClassificationRepositoryPort = accountClassificationRepositoryPort;
    }

    public List<Account> getAllAccounts(GetAllAccountCommand command){
        try {
            log.debug("Starting Get All Accounts");
            List<Account> results;
            Sort sort = null;
            Specification<AccountEntity> spec = Specification.where(belongsToUser(command.userId()));

            if(command.sort().isPresent()){
                sort = Sort.by(command.sort().get().direction() == SortDirection.ascending? Sort.Direction.ASC : Sort.Direction.DESC, command.sort().get().type().toString());
            }

            if(command.filters().isPresent()){
                var f = command.filters().get();
                boolean useSearch = f.searchContains().isPresent()
                        && f.searchContains().get() != null
                        && !f.searchContains().get().isBlank();
                if (useSearch) {
                    spec = spec
                            .and(searchContainsDescriptionOrNotes(f.searchContains().get().trim()))
                            .and(typeIdWithin(f.accountTypes().orElse(null)))
                            .and(hideInactive(f.hideInactive().orElse(null)))
                            .and(hideActive(f.hideActive().orElse(null)));
                } else {
                    spec = spec
                            .and(descriptionContains(f.descriptionContains().orElse(null)))
                            .and(notesContains(f.notesContains().orElse(null)))
                            .and(typeIdWithin(f.accountTypes().orElse(null)))
                            .and(hideInactive(f.hideInactive().orElse(null)))
                            .and(hideActive(f.hideActive().orElse(null)));
                }
            }

            if(sort != null){
                results = accountRepository.findAll(command.userId(), spec, sort);
            } else {
                results = accountRepository.findAll(command.userId(), spec);
            }

            log.debug("Ending Get All Accounts results:[{}]", results.size());
            return results;
        } catch (RuntimeException ex) {
            log.error("AccountService.getAllAccounts failed", ex);
            throw ex;
        }
    }

    @Override
    public Account getAccountById(GetByIdAccountCommand command){
        try {
            log.debug("Starting Get Account by id:[{}]", command.id());
            Account result = accountRepository.findById(command.userId(), command.id());
            log.debug("Ending Get Account by id:[{}]", result.id());
            return result;
        } catch (RuntimeException ex) {
            log.error("AccountService.getAccountById failed for command:[{}]", command, ex);
            throw ex;
        }
    }

    @Override
    public Account createAccount(CreateAccountCommand command) {
        try {
            log.debug("Starting Create Account typeId:[{}] description:[{}]", command.typeId(), command.description());
            Account account = Account.createNew(command.typeId(), command.description(), command.notes().orElse(""), command.userId());
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
            Account account = accountRepository.findById(command.userId(), command.id());

            Account newAccount = Account.rehydrate(
                    account.id(),
                    account.typeId(),
                    command.description().orElse(account.description()),
                    command.active().orElse(account.active()),
                    command.notes().orElse(account.notes()),
                    account.getUserId()
                    );

            if (account.active() && !newAccount.active()) {
                AccountType type = accountTypeRepositoryPort.findByIdVisibleToUser(command.userId(), account.typeId());
                AccountClassification classification = accountClassificationRepositoryPort.findById(type.classificationId());
                Long balance = getBalanceForAccountUseCase.getBalanceForAccount(command.userId(), account.id(), classification);
                if (balance != null && balance != 0L) {
                    throw new ApplicationException("Cannot deactivate an account while its balance is not zero.");
                }
            }

            // Check for unique description
            if(command.description().isPresent() && accountRepository.existsByDescription(command.userId(), newAccount.description()) ){
                Account existing = accountRepository.findByDescription(command.userId(), newAccount.description());
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
