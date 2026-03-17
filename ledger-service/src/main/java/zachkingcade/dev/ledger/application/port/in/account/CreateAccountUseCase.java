package zachkingcade.dev.ledger.application.port.in.account;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.application.commands.account.CreateAccountCommand;
import zachkingcade.dev.ledger.domain.account.Account;

@Service
public interface CreateAccountUseCase {

    Account createAccount(CreateAccountCommand command);
}
