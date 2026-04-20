package zachkingcade.dev.ledger.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.application.exception.NotFoundException;
import zachkingcade.dev.ledger.application.port.in.pendingtransaction.GetAllPendingTransactionsUseCase;
import zachkingcade.dev.ledger.application.port.in.pendingtransaction.RemovePendingTransactionUseCase;
import zachkingcade.dev.ledger.application.port.out.pendingtransaction.PendingTransactionRepositoryPort;
import zachkingcade.dev.ledger.domain.pendingtransaction.PendingTransaction;

import java.util.List;

@Service
public class PendingTransactionService implements GetAllPendingTransactionsUseCase, RemovePendingTransactionUseCase {

    private static final Logger log = LoggerFactory.getLogger(PendingTransactionService.class);

    private final PendingTransactionRepositoryPort pendingTransactionRepository;

    public PendingTransactionService(PendingTransactionRepositoryPort pendingTransactionRepository) {
        this.pendingTransactionRepository = pendingTransactionRepository;
    }

    @Override
    public List<PendingTransaction> getAllPendingTransactionsForUser(Long userId) {
        try {
            log.debug("Starting Get All Pending Transactions userId:[{}]", userId);
            List<PendingTransaction> results = pendingTransactionRepository.findAllByUserId(userId);
            log.debug("Ending Get All Pending Transactions userId:[{}] count:[{}]", userId, results.size());
            return results;
        } catch (RuntimeException ex) {
            log.error("PendingTransactionService.getAllPendingTransactionsForUser failed for userId:[{}]", userId, ex);
            throw ex;
        }
    }

    @Override
    public void removePendingTransaction(Long userId, Long transactionNumber) {
        try {
            log.debug("Starting Remove Pending Transaction userId:[{}] transactionNumber:[{}]", userId, transactionNumber);
            boolean existsForUser = pendingTransactionRepository
                    .findByTransactionNumberAndUserId(transactionNumber, userId)
                    .isPresent();
            if (!existsForUser) {
                throw new NotFoundException(String.format("Pending Transaction not found for id [%s]", transactionNumber));
            }
            pendingTransactionRepository.deleteByTransactionNumber(transactionNumber);
            log.debug("Ending Remove Pending Transaction userId:[{}] transactionNumber:[{}]", userId, transactionNumber);
        } catch (RuntimeException ex) {
            log.error("PendingTransactionService.removePendingTransaction failed for userId:[{}] transactionNumber:[{}]", userId, transactionNumber, ex);
            throw ex;
        }
    }
}

