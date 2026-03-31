package zachkingcade.dev.ledger.application.port.in.accountclassification;

import zachkingcade.dev.ledger.domain.account.AccountClassification;

public interface GetByIdAccountClassificationUseCase {
    AccountClassification getByIdAccountClassification(Long id);
}
