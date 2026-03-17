package zachkingcade.dev.ledger.application;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.application.port.in.accountclassification.GetAllAccountClassifcationsUseCase;
import zachkingcade.dev.ledger.application.port.in.accountclassification.GetByIdAccountClassificaitonUseCase;
import zachkingcade.dev.ledger.application.port.out.type.AccountClassificationRepositoryPort;
import zachkingcade.dev.ledger.domain.account.AccountClassification;

import java.util.List;

@Service
public class AccountClassificationService implements GetAllAccountClassifcationsUseCase, GetByIdAccountClassificaitonUseCase {

    private final AccountClassificationRepositoryPort accountClassificationRepository;

    public AccountClassificationService(AccountClassificationRepositoryPort accountClassificationRepository) {
        this.accountClassificationRepository = accountClassificationRepository;
    }

    @Override
    public List<AccountClassification> getAllAccountClassifications() {
        return accountClassificationRepository.findAll();
    }

    @Override
    public AccountClassification getByIdAccountClassifcation(Long id) {
        return accountClassificationRepository.findById(id);
    }
}
