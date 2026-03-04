package zachkingcade.dev.ledger.application.abstracts;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.out.persistence.AccountJpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.AccountTypeJpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;
import zachkingcade.dev.ledger.application.commands.CreateAccountCommand;
import zachkingcade.dev.ledger.domain.account.Account;

@Service
public interface CreateAccountUseCase {

    public Account createAccount(CreateAccountCommand command) throws Exception;
}
