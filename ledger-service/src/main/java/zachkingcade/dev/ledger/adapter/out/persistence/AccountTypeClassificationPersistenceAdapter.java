package zachkingcade.dev.ledger.adapter.out.persistence;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountClassificationEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.AccountClassificationJpaRepository;
import zachkingcade.dev.ledger.application.port.out.type.AccountTypeClassificationRepositoryPort;
import zachkingcade.dev.ledger.domain.account.AccountClassification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AccountTypeClassificationPersistenceAdapter implements AccountTypeClassificationRepositoryPort {

    AccountClassificationJpaRepository accountClassificationJpaRepository;

    public AccountTypeClassificationPersistenceAdapter(AccountClassificationJpaRepository accountClassificationJpaRepository) {
        this.accountClassificationJpaRepository = accountClassificationJpaRepository;
    }

    @Override
    public AccountClassification findById(Long id) {
        Optional<AccountClassificationEntity> entity = accountClassificationJpaRepository.findById(id);
        if(entity.isPresent()){
            return AccountClassification.rehydrate(entity.get().getId(), entity.get().getDescription(), entity.get().getCreditEffect(), entity.get().getDebitEffect());
        } else {
            throw new RuntimeException(String.format("Error: Account Type Classification not found for id [%s]", id));
        }
    }

    @Override
    public List<AccountClassification> findAll() {
        List<AccountClassificationEntity> accountClassificationList = accountClassificationJpaRepository.findAll();

        List<AccountClassification> resultingList = new ArrayList<>();
        for(AccountClassificationEntity entity : accountClassificationList){
            resultingList.add(AccountClassification.rehydrate(entity.getId(), entity.getDescription(), entity.getCreditEffect(), entity.getCreditEffect()));
        }

        return resultingList;
    }
}
