package zachkingcade.dev.ledger.application.port.in.account;

import zachkingcade.dev.ledger.application.commands.account.CreateAccountCommand;
import zachkingcade.dev.ledger.domain.account.Account;

public interface CreateAccountUseCase {

    Account createAccount(CreateAccountCommand command);
}
