package zachkingcade.dev.ledger.application.port.in.account;

import zachkingcade.dev.ledger.domain.account.Account;

import java.util.List;

public interface GetallAccountsUseCase {
    List<Account> getAllAccounts();
}
