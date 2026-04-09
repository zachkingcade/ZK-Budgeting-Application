package zachkingcade.dev.ledger.application.port.in.account;

import zachkingcade.dev.ledger.domain.account.Account;
import zachkingcade.dev.ledger.application.commands.account.GetByIdAccountCommand;

public interface GetByIdAccountUseCase {
    Account getAccountById(GetByIdAccountCommand command);
}
