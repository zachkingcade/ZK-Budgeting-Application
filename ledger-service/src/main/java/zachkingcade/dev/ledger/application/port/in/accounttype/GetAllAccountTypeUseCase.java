package zachkingcade.dev.ledger.application.port.in.accounttype;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.domain.account.AccountType;

import java.util.List;

@Service
public interface GetAllAccountTypeUseCase {
    public List<AccountType> getAllAccountTypes();
}
