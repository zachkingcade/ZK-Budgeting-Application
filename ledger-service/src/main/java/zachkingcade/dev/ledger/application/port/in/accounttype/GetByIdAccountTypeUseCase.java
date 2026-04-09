package zachkingcade.dev.ledger.application.port.in.accounttype;

import zachkingcade.dev.ledger.domain.account.AccountType;
import zachkingcade.dev.ledger.application.commands.accounttype.GetByIdAccountTypeCommand;

public interface GetByIdAccountTypeUseCase {
    AccountType getAccountTypeById(GetByIdAccountTypeCommand command);
}
