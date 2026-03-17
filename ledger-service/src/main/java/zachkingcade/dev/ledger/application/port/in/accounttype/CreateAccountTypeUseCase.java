package zachkingcade.dev.ledger.application.port.in.accounttype;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.application.commands.accounttype.CreateAccountTypeCommand;
import zachkingcade.dev.ledger.domain.account.AccountType;

@Service
public interface CreateAccountTypeUseCase {
    public AccountType createAccountType(CreateAccountTypeCommand command);
}
