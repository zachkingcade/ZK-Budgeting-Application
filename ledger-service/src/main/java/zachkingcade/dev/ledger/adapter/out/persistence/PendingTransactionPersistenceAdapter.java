package zachkingcade.dev.ledger.adapter.out.persistence;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.PendingTransactionEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.PendingTransactionJpaRepository;
import zachkingcade.dev.ledger.application.port.out.pendingtransaction.PendingTransactionRepositoryPort;
import zachkingcade.dev.ledger.domain.pendingtransaction.PendingTransaction;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PendingTransactionPersistenceAdapter implements PendingTransactionRepositoryPort {

    private final PendingTransactionJpaRepository pendingTransactionJpaRepository;

    public PendingTransactionPersistenceAdapter(PendingTransactionJpaRepository pendingTransactionJpaRepository) {
        this.pendingTransactionJpaRepository = pendingTransactionJpaRepository;
    }

    @Override
    public List<PendingTransaction> findAllByUserId(Long userId) {
        List<PendingTransactionEntity> entities = pendingTransactionJpaRepository.findAllByUserIdOrderByTransactionDateDescTransactionNumberDesc(userId);
        List<PendingTransaction> results = new ArrayList<>();
        for (PendingTransactionEntity e : entities) {
            results.add(mapToDomain(e));
        }
        return results;
    }

    @Override
    public Optional<PendingTransaction> findByTransactionNumberAndUserId(Long transactionNumber, Long userId) {
        return pendingTransactionJpaRepository.findByTransactionNumberAndUserId(transactionNumber, userId).map(this::mapToDomain);
    }

    @Override
    public void deleteByTransactionNumber(Long transactionNumber) {
        pendingTransactionJpaRepository.deleteById(transactionNumber);
    }

    @Override
    public PendingTransaction save(PendingTransaction pendingTransaction) {
        PendingTransactionEntity entity = new PendingTransactionEntity();
        if (pendingTransaction.transactionNumber() != null) {
            entity.setTransactionNumber(pendingTransaction.transactionNumber());
        }
        entity.setUserId(pendingTransaction.userId());
        entity.setTransactionDate(Date.valueOf(pendingTransaction.transactionDate()));
        entity.setDescription(pendingTransaction.description());
        entity.setAmount(pendingTransaction.amount());
        entity.setNotes(pendingTransaction.notes() == null ? "" : pendingTransaction.notes());

        PendingTransactionEntity saved = pendingTransactionJpaRepository.save(entity);
        return mapToDomain(saved);
    }

    private PendingTransaction mapToDomain(PendingTransactionEntity e) {
        return new PendingTransaction(
                e.getTransactionNumber(),
                e.getUserId(),
                e.getTransactionDate().toLocalDate(),
                e.getDescription(),
                e.getAmount(),
                e.getNotes()
        );
    }
}

