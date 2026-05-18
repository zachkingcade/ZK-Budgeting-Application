package zachkingcade.dev.ledger.application.port.in.pendingtransaction;

import java.time.LocalDate;

public interface UpdatePendingTransactionDateUseCase {
    void updatePendingTransactionDate(Long userId, Long transactionNumber, LocalDate transactionDate);
}
