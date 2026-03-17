package zachkingcade.dev.ledger.application.port.out.type;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.domain.account.AccountClassification;

import java.util.List;
import java.util.Optional;

@Service
public interface AccountTypeClassificationRepositoryPort {

    public AccountClassification findById(Long id);

    List<AccountClassification> findAll();
}
