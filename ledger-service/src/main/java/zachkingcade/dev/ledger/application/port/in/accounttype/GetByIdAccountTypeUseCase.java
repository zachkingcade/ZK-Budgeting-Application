package zachkingcade.dev.ledger.application.port.in.accounttype;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.domain.account.AccountType;

@Service
public interface GetByIdAccountTypeUseCase {
    public AccountType getAccountTypeById(Long id);
}
