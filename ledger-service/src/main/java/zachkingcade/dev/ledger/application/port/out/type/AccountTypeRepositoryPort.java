package zachkingcade.dev.ledger.application.port.out.type;

import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountTypeEntity;

import java.util.Optional;

public interface AccountTypeRepositoryPort {

    public Optional<AccountTypeEntity> findById(Long id);
}
