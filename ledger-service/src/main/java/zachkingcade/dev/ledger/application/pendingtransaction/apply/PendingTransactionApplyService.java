package zachkingcade.dev.ledger.application.pendingtransaction.apply;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import zachkingcade.dev.ledger.application.commands.journal.CreateJournalEntryCommand;
import zachkingcade.dev.ledger.application.commands.journal.JournalLineCommandObject;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.exception.NotFoundException;
import zachkingcade.dev.ledger.application.port.in.journal.CreateJournalEntryUseCase;
import zachkingcade.dev.ledger.application.port.in.pendingtransaction.ApplyPendingTransactionsUseCase;
import zachkingcade.dev.ledger.application.port.out.pendingtransaction.PendingTransactionRepositoryPort;
import zachkingcade.dev.ledger.domain.journal.JournalEntry;
import zachkingcade.dev.ledger.domain.pendingtransaction.PendingTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PendingTransactionApplyService implements ApplyPendingTransactionsUseCase {

    private static final Logger log = LoggerFactory.getLogger(PendingTransactionApplyService.class);

    private final PendingTransactionRepositoryPort pendingTransactionRepository;
    private final CreateJournalEntryUseCase createJournalEntryUseCase;
    private final TransactionTemplate requiresNew;

    public PendingTransactionApplyService(
            PendingTransactionRepositoryPort pendingTransactionRepository,
            CreateJournalEntryUseCase createJournalEntryUseCase,
            PlatformTransactionManager transactionManager
    ) {
        this.pendingTransactionRepository = pendingTransactionRepository;
        this.createJournalEntryUseCase = createJournalEntryUseCase;
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.requiresNew = tt;
    }

    @Override
    public ApplyPendingTransactionsResult applyPendingTransactions(ApplyPendingTransactionsCommand command) {
        try {
            if (command == null || command.userId() == null) {
                throw new IllegalArgumentException("userId is required.");
            }
            if (command.items() == null || command.items().isEmpty()) {
                throw new IllegalArgumentException("At least one item is required.");
            }

            Long userId = command.userId();
            log.debug("Starting Apply Pending Transactions userId:[{}] itemCount:[{}]", userId, command.items().size());

            List<ApplyPendingTransactionsResult.Succeeded> succeeded = new ArrayList<>();
            List<ApplyPendingTransactionsResult.Failed> failed = new ArrayList<>();

            for (ApplyPendingTransactionsCommand.Item item : command.items()) {
                ApplyPendingTransactionsResult.Succeeded ok = null;
                String err = null;

                try {
                    ok = requiresNew.execute((_status) -> applyOne(userId, item));
                } catch (RuntimeException ex) {
                    err = ex.getMessage() == null ? "Failed to apply pending transaction." : ex.getMessage();
                }

                if (ok != null) {
                    succeeded.add(ok);
                } else {
                    failed.add(new ApplyPendingTransactionsResult.Failed(item.pendingTransactionNumber(), err));
                }
            }

            ApplyPendingTransactionsResult result = new ApplyPendingTransactionsResult(
                    succeeded.size(),
                    failed.size(),
                    succeeded,
                    failed
            );

            log.debug("Ending Apply Pending Transactions userId:[{}] success:[{}] failed:[{}]", userId, result.successCount(), result.failureCount());
            return result;
        } catch (RuntimeException ex) {
            log.error("PendingTransactionApplyService.applyPendingTransactions failed", ex);
            throw ex;
        }
    }

    private ApplyPendingTransactionsResult.Succeeded applyOne(Long userId, ApplyPendingTransactionsCommand.Item item) {
        if (item == null || item.pendingTransactionNumber() == null) {
            throw new IllegalArgumentException("pendingTransactionNumber is required.");
        }
        if (item.entryDate() == null) {
            throw new IllegalArgumentException("entryDate is required.");
        }
        String desc = item.description() == null ? "" : item.description().trim();
        if (desc.isBlank()) {
            throw new IllegalArgumentException("description is required.");
        }
        if (item.journalLines() == null || item.journalLines().isEmpty()) {
            throw new IllegalArgumentException("journalLines is required.");
        }

        PendingTransaction pending = pendingTransactionRepository
                .findByTransactionNumberAndUserId(item.pendingTransactionNumber(), userId)
                .orElseThrow(() -> new NotFoundException(String.format("Pending Transaction not found for id [%s]", item.pendingTransactionNumber())));

        long debit = 0;
        long credit = 0;
        List<JournalLineCommandObject> lineCommands = new ArrayList<>();
        for (var l : item.journalLines()) {
            if (l == null) {
                continue;
            }
            if (l.amount() == null || l.amount() < 1) {
                throw new ApplicationException("JournalLine requires a positive non zero amount");
            }
            if (l.direction() != 'D' && l.direction() != 'C') {
                throw new ApplicationException("JournalLine requires a direction of either C or D");
            }
            if (l.accountId() == null) {
                throw new ApplicationException("JournalLine requires accountId");
            }
            if (l.direction() == 'D') {
                debit += l.amount();
            } else {
                credit += l.amount();
            }
            Optional<String> notes = l.notes() == null ? Optional.empty() : l.notes();
            lineCommands.add(new JournalLineCommandObject(
                    l.amount(),
                    l.accountId(),
                    l.direction(),
                    notes
            ));
        }

        Long pendingAmount = pending.amount();
        if (pendingAmount == null || pendingAmount < 1) {
            throw new ApplicationException("Pending transaction amount is invalid.");
        }
        if (debit != credit || debit != pendingAmount) {
            throw new ApplicationException(String.format(
                    "Journal lines must balance and match pending amount. Pending:[%s] Debit:[%s] Credit:[%s]",
                    pendingAmount,
                    debit,
                    credit
            ));
        }

        CreateJournalEntryCommand createCommand = new CreateJournalEntryCommand(
                userId,
                item.entryDate(),
                desc,
                item.notes(),
                lineCommands
        );

        JournalEntry created = createJournalEntryUseCase.createJournalEntry(createCommand);

        pendingTransactionRepository.deleteByTransactionNumber(item.pendingTransactionNumber());

        return new ApplyPendingTransactionsResult.Succeeded(item.pendingTransactionNumber(), created.id());
    }
}

