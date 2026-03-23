package zachkingcade.dev.ledger.application.port.in.account;

import zachkingcade.dev.ledger.application.commands.account.GetAllAccountCommand;
import zachkingcade.dev.ledger.domain.account.Account;

import java.util.List;

public interface GetAllAccountsUseCase {
    List<Account> getAllAccounts(GetAllAccountCommand command);
}
