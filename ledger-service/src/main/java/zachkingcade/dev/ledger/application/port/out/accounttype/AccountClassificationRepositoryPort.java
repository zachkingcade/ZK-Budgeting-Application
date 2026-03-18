package zachkingcade.dev.ledger.application.port.out.accounttype;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.domain.account.AccountClassification;

import java.util.List;

@Service
public interface AccountClassificationRepositoryPort {

    public AccountClassification findById(Long id);

    List<AccountClassification> findAll();
}
