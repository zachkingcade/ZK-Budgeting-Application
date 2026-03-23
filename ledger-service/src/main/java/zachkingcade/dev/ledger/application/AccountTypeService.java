package zachkingcade.dev.ledger.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.application.commands.accounttype.CreateAccountTypeCommand;
import zachkingcade.dev.ledger.application.commands.accounttype.GetAllAccountTypesCommand;
import zachkingcade.dev.ledger.application.commands.accounttype.UpdateAccountTypeCommand;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.port.in.accounttype.CreateAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.in.accounttype.GetAllAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.in.accounttype.GetByIdAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.in.accounttype.UpdateAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.out.accounttype.AccountClassificationRepositoryPort;
import zachkingcade.dev.ledger.application.port.out.accounttype.AccountTypeRepositoryPort;
import zachkingcade.dev.ledger.application.validation.SortDirection;
import zachkingcade.dev.ledger.domain.account.Account;
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
    public List<AccountType> getAllAccountTypes(GetAllAccountTypesCommand command) {
        try {
            log.debug("Starting Get All Account Types");
            List<AccountType> results;
            if(command.sort().isPresent()){
                Sort sort = Sort.by(command.sort().get().direction() == SortDirection.ascending? Sort.Direction.ASC : Sort.Direction.DESC, command.sort().get().type().toString());
                results = accountTypeRepository.findAll(sort);
            } else {
                results = accountTypeRepository.findAll();
            }
            log.debug("Ending Get All Account Types results:[{}]", results.size());
            return results;
        } catch (RuntimeException ex) {
            log.error("AccountTypeService.getAllAccountTypes failed", ex);
            throw ex;
        }
    }

    @Override
    public AccountType getAccountTypeById(Long id) {
        try {
            log.debug("Starting Get Account Type by id:[{}]",id);
            AccountType result = accountTypeRepository.findById(id);
            log.debug("Ending Get Account Type by id:[{}]", result.id());
            return result;
        } catch (RuntimeException ex) {
            log.error("AccountTypeService.getAccountTypeById failed for id:[{}]", id, ex);
            throw ex;
        }
    }

    @Override
    public AccountType createAccountType(CreateAccountTypeCommand command) {
        try {
            log.debug("Starting Create Account Type classificationId:[{}] description:[{}]",command.classificationId(),command.description());
            AccountType accountType = AccountType.createNew(command.description(), command.classificationId(), command.notes().orElse(""));
            AccountType saved = accountTypeRepository.save(accountType);
            log.debug("Ending Create Account Type createdId:[{}]",saved.id());
            return saved;
        } catch (RuntimeException ex) {
            log.error("AccountTypeService.createAccountType failed for command:[{}]", command, ex);
            throw ex;
        }
    }

    @Override
    public AccountType updateAccountType(UpdateAccountTypeCommand command) {
        try {
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
        } catch (RuntimeException ex) {
            log.error("AccountTypeService.updateAccountType failed for command:[{}]", command, ex);
            throw ex;
        }
    }
}
