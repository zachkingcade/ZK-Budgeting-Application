package zachkingcade.dev.ledger.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.application.commands.accounttype.CreateAccountTypeCommand;
import zachkingcade.dev.ledger.application.commands.accounttype.UpdateAccountTypeCommand;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.port.in.accounttype.CreateAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.in.accounttype.GetAllAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.in.accounttype.GetByIdAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.in.accounttype.UpdateAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.out.accounttype.AccountClassificationRepositoryPort;
import zachkingcade.dev.ledger.application.port.out.accounttype.AccountTypeRepositoryPort;
import zachkingcade.dev.ledger.domain.account.AccountType;

import java.util.List;

@Service
public class AccountTypeService implements CreateAccountTypeUseCase, GetAllAccountTypeUseCase, GetByIdAccountTypeUseCase, UpdateAccountTypeUseCase {

    AccountTypeRepositoryPort accountTypeRepository;
    AccountClassificationRepositoryPort accountClassificationRepository;
    private static final Logger log = LoggerFactory.getLogger(AccountTypeService.class);

    public AccountTypeService(AccountTypeRepositoryPort accountTypeRepository, AccountClassificationRepositoryPort accountClassificationRepository) {
        this.accountTypeRepository = accountTypeRepository;
        this.accountClassificationRepository = accountClassificationRepository;
    }

    @Override
    public List<AccountType> getAllAccountTypes() {
        log.debug("Starting Get All Account Types");
        List<AccountType> results = accountTypeRepository.findAll();
        log.debug("Ending Get All Account Types results:[{}]", results.size());
        return results;
    }

    @Override
    public AccountType getAccountTypeById(Long id) {
        log.debug("Starting Get Account Type by id:[{}]",id);
        AccountType result = accountTypeRepository.findById(id);
        log.debug("Ending Get Account Type by id:[{}]", result.id());
        return result;
    }

    @Override
    public AccountType createAccountType(CreateAccountTypeCommand command) {
        log.debug("Starting Create Account Type classificationId:[{}] description:[{}]",command.classificationId(),command.description());
        AccountType accountType = AccountType.createNew(command.description(), command.classificationId(), command.notes().orElse(""));
        AccountType saved = accountTypeRepository.save(accountType);
        log.debug("Ending Create Account Type createdId:[{}]",saved.id());
        return saved;
    }

    @Override
    public AccountType updateAccountType(UpdateAccountTypeCommand command) {
        log.debug("Starting Update Account Type accountTypeId:[{}] description:[{}]",command.id(),command.description());
        AccountType accountType = accountTypeRepository.findById(command.id());

        AccountType newAccountType = AccountType.rehydrate(
                accountType.id(),
                command.description().orElse(accountType.description()),
                accountType.classificationId(),
                command.notes().orElse(accountType.notes()),
                command.active().orElse(accountType.active())
        );

        // Check for unique description
        if(command.description().isPresent() && accountTypeRepository.existsByDescription(newAccountType.description()) ){
            AccountType existing = accountTypeRepository.findByDescription(newAccountType.description());
            if(!existing.id().equals(newAccountType.id()) ){
                log.debug("Update Account Type unique-description validation failed description:[{}] existingId:[{}] currentId:[{}]",newAccountType.description(),existing.id(),newAccountType.id());
                throw new ApplicationException(String.format("An account type already exists with the description: [%s]", newAccountType.description()));
            }
        }

        AccountType saved = accountTypeRepository.save(newAccountType);
        log.debug("Ending Update Account Type updatedId:[{}]",saved.id());
        return saved;
    }
}
