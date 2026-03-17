package zachkingcade.dev.ledger.application.port.in.accountclassification;

import zachkingcade.dev.ledger.domain.account.AccountClassification;

import java.util.List;

public interface GetAllAccountClassifcationsUseCase {
    List<AccountClassification> getAllAccountClassifications();
}
