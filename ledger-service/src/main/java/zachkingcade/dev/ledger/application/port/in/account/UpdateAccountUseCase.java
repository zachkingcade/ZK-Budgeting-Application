package zachkingcade.dev.ledger.application.port.in.account;

import zachkingcade.dev.ledger.application.commands.account.UpdateAccountCommand;
import zachkingcade.dev.ledger.domain.account.Account;

public interface UpdateAccountUseCase {
    Account updateAccount(UpdateAccountCommand command);
}
