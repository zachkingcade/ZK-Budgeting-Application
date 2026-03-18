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
        return accountTypeRepository.findAll();
    }

    @Override
    public AccountType getAccountTypeById(Long id) {
        return accountTypeRepository.findById(id);
    }

    @Override
    public AccountType createAccountType(CreateAccountTypeCommand command) {
        log.info("Starting Create Account Type for new type [{}]", command.description());
        AccountType accountType = AccountType.createNew(command.description(), command.classificationId(), command.notes().orElse(""));
        return accountTypeRepository.save(accountType);
    }

    @Override
    public AccountType updateAccountType(UpdateAccountTypeCommand command) {
        log.info("Starting Update Account Type for type [{}][{}]", command.id(),command.description());
        AccountType accountType = accountTypeRepository.findById(command.id());

        AccountType newAccountType = AccountType.rehydrate(
                accountType.id(),
                command.description().orElse(accountType.description()),
                accountType.classificationId(),
                command.notes().orElse(accountType.notes()),
                command.active().orElse(accountType.active())
        );

        // Check for unique description
        if(command.description().isPresent() && accountTypeRepository.existsByDescription(newAccountType.description()) && !accountTypeRepository.findByDescription(newAccountType.description()).id().equals(newAccountType.id()) ){
            throw new ApplicationException(String.format("An account type already exists with the description: [%s]", newAccountType.description()));
        }

        return accountTypeRepository.save(newAccountType);
    }
}
