package zachkingcade.dev.ledger.application.port.in.accounttype;

import zachkingcade.dev.ledger.domain.account.AccountType;

public interface GetByIdAccountTypeUseCase {
    public AccountType getAccountTypeById(Long id);
}
