package zachkingcade.dev.ledger.application.port.in.accounttype;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.application.commands.accounttype.UpdateAccountTypeCommand;
import zachkingcade.dev.ledger.domain.account.AccountType;

@Service
public interface UpdateAccountTypeUseCase {
    public AccountType updateAccountType(UpdateAccountTypeCommand command);
}
