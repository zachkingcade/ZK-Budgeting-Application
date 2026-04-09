package zachkingcade.dev.ledger.application.port.in.journal;

import zachkingcade.dev.ledger.domain.account.AccountClassification;

public interface GetBalanceForAccountUseCase {
    Long getBalanceForAccount(Long userId, Long accountId, AccountClassification classification);
}
