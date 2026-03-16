package zachkingcade.dev.ledger.application.port.in.account;

import zachkingcade.dev.ledger.application.commands.account.GetByIdAccountCommand;
import zachkingcade.dev.ledger.application.commands.account.GetByIdAccountCommand;
import zachkingcade.dev.ledger.domain.account.Account;

public interface GetByIdAccountUseCase {
    Account getAccountById(GetByIdAccountCommand command);
}
