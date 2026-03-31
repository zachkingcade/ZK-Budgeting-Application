package zachkingcade.dev.ledger.application.port.in.accounttype;

import zachkingcade.dev.ledger.application.commands.accounttype.GetAllAccountTypesCommand;
import zachkingcade.dev.ledger.domain.account.AccountType;

import java.util.List;

public interface GetAllAccountTypeUseCase {
    public List<AccountType> getAllAccountTypes(GetAllAccountTypesCommand command);
}
