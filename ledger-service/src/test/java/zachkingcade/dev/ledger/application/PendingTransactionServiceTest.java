package zachkingcade.dev.ledger.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zachkingcade.dev.ledger.application.exception.NotFoundException;
import zachkingcade.dev.ledger.application.port.out.pendingtransaction.PendingTransactionRepositoryPort;
import zachkingcade.dev.ledger.domain.pendingtransaction.PendingTransaction;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PendingTransactionServiceTest {

    @Mock
    private PendingTransactionRepositoryPort pendingTransactionRepository;

    @InjectMocks
    private PendingTransactionService service;

    @Test
    void updatePendingTransactionDatePersistsNewDate() {
        PendingTransaction existing = new PendingTransaction(5L, 1L, LocalDate.of(2026, 1, 15), "Coffee", 500L, "");
        when(pendingTransactionRepository.findByTransactionNumberAndUserId(5L, 1L)).thenReturn(Optional.of(existing));

        service.updatePendingTransactionDate(1L, 5L, LocalDate.of(2026, 2, 1));

        ArgumentCaptor<PendingTransaction> captor = ArgumentCaptor.forClass(PendingTransaction.class);
        verify(pendingTransactionRepository).save(captor.capture());
        assertEquals(LocalDate.of(2026, 2, 1), captor.getValue().transactionDate());
    }

    @Test
    void updatePendingTransactionDateNotFoundThrows() {
        when(pendingTransactionRepository.findByTransactionNumberAndUserId(9L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.updatePendingTransactionDate(1L, 9L, LocalDate.now()));
    }
}
