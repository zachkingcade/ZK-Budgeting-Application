package zachkingcade.dev.ledger.application.port.in.accounttype;

import zachkingcade.dev.ledger.application.commands.accounttype.UpdateAccountTypeCommand;
import zachkingcade.dev.ledger.domain.account.AccountType;

public interface UpdateAccountTypeUseCase {
    public AccountType updateAccountType(UpdateAccountTypeCommand command);
}
