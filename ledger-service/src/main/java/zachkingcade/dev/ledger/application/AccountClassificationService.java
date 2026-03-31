package zachkingcade.dev.ledger.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.application.port.in.accountclassification.GetAllAccountClassificationsUseCase;
import zachkingcade.dev.ledger.application.port.in.accountclassification.GetByIdAccountClassificationUseCase;
import zachkingcade.dev.ledger.application.port.out.accounttype.AccountClassificationRepositoryPort;
import zachkingcade.dev.ledger.domain.account.AccountClassification;

import java.util.List;

@Service
public class AccountClassificationService implements GetAllAccountClassificationsUseCase, GetByIdAccountClassificationUseCase {

    private final AccountClassificationRepositoryPort accountClassificationRepository;
    private static final Logger log = LoggerFactory.getLogger(AccountClassificationService.class);

    public AccountClassificationService(AccountClassificationRepositoryPort accountClassificationRepository) {
        this.accountClassificationRepository = accountClassificationRepository;
    }

    @Override
    public List<AccountClassification> getAllAccountClassifications() {
        try {
            log.debug("Starting Get All Account Classifications");
            List<AccountClassification> results = accountClassificationRepository.findAll();
            log.debug("Ending Get All Account Classifications results:[{}]",results.size());
            return results;
        } catch (RuntimeException ex) {
            log.error("AccountClassificationService.getAllAccountClassifications failed", ex);
            throw ex;
        }
    }

    @Override
    public AccountClassification getByIdAccountClassification(Long id) {
        try {
            log.debug("Starting Get Account Classification by id:[{}]",id);
            AccountClassification result = accountClassificationRepository.findById(id);
            log.debug("Ending Get Account Classification by id:[{}]",result.id());
            return result;
        } catch (RuntimeException ex) {
            log.error("AccountClassificationService.getByIdAccountClassification failed for id:[{}]", id, ex);
            throw ex;
        }
    }
}
