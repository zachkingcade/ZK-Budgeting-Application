package zachkingcade.dev.ledger.application.port.in.journal;

import zachkingcade.dev.ledger.domain.account.AccountClassification;

public interface GetBalanceForAccountUseCase {
    public Long getBalanceForAccount(Long accountId, AccountClassification classification);
}
