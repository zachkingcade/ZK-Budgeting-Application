package zachkingcade.dev.ledger.application.port.out.accounttype;

import zachkingcade.dev.ledger.domain.account.AccountClassification;

import java.util.List;

public interface AccountClassificationRepositoryPort {

    public AccountClassification findById(Long id);

    List<AccountClassification> findAll();
}
