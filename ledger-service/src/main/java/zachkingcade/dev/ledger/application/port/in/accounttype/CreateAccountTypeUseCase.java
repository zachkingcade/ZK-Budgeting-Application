package zachkingcade.dev.ledger.application.port.in.accounttype;

import zachkingcade.dev.ledger.application.commands.accounttype.CreateAccountTypeCommand;
import zachkingcade.dev.ledger.domain.account.AccountType;

public interface CreateAccountTypeUseCase {
    public AccountType createAccountType(CreateAccountTypeCommand command);
}
