package zachkingcade.dev.ledger.application.pendingtransaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.exception.NotFoundException;
import zachkingcade.dev.ledger.application.importtypes.ImportFormatDetails;
import zachkingcade.dev.ledger.application.importtypes.ImportType;
import zachkingcade.dev.ledger.application.importtypes.PendingTransactionDraft;
import zachkingcade.dev.ledger.application.port.in.pendingtransaction.ImportPendingTransactionsUseCase;
import zachkingcade.dev.ledger.application.port.out.importformat.ImportFormatRepositoryPort;
import zachkingcade.dev.ledger.application.port.out.pendingtransaction.PendingTransactionRepositoryPort;
import zachkingcade.dev.ledger.domain.importformat.ImportFormat;
import zachkingcade.dev.ledger.domain.pendingtransaction.PendingTransaction;

import java.io.InputStream;
import java.util.List;

@Service
public class PendingTransactionImportService implements ImportPendingTransactionsUseCase {

    private static final Logger log = LoggerFactory.getLogger(PendingTransactionImportService.class);

    private final ImportFormatRepositoryPort importFormatRepository;
    private final PendingTransactionRepositoryPort pendingTransactionRepository;
    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

    public PendingTransactionImportService(
            ImportFormatRepositoryPort importFormatRepository,
            PendingTransactionRepositoryPort pendingTransactionRepository,
            ApplicationContext applicationContext,
            ObjectMapper objectMapper
    ) {
        this.importFormatRepository = importFormatRepository;
        this.pendingTransactionRepository = pendingTransactionRepository;
        this.applicationContext = applicationContext;
        this.objectMapper = objectMapper;
    }

    @Override
    public ImportPendingTransactionsResult importPendingTransactions(Long userId, Long formatId, InputStream inputStream) {
        try {
            log.debug("Starting Import Pending Transactions userId:[{}] formatId:[{}]", userId, formatId);
            if (inputStream == null) {
                throw new ApplicationException("Missing import file.");
            }

            ImportFormat format = importFormatRepository
                    .findById(formatId)
                    .orElseThrow(() -> new NotFoundException(String.format("Import format not found for id [%s]", formatId)));

            if (!format.active()) {
                throw new ApplicationException("Selected import format is inactive.");
            }

            ImportFormatDetails details = parseDetails(format.formatDetails());
            ImportType importType = resolveImportBean(format.beanName());

            List<PendingTransactionDraft> drafts = importType.parse(inputStream, details);
            long created = 0;
            for (PendingTransactionDraft d : drafts) {
                PendingTransaction toSave = new PendingTransaction(
                        null,
                        userId,
                        d.transactionDate(),
                        d.description(),
                        d.amountMinorUnits(),
                        d.notes() == null ? "" : d.notes()
                );
                pendingTransactionRepository.save(toSave);
                created++;
            }

            log.debug("Ending Import Pending Transactions userId:[{}] formatId:[{}] created:[{}]", userId, formatId, created);
            return new ImportPendingTransactionsResult(created);
        } catch (RuntimeException ex) {
            log.error("PendingTransactionImportService.importPendingTransactions failed userId:[{}] formatId:[{}]", userId, formatId, ex);
            throw ex;
        }
    }

    private ImportFormatDetails parseDetails(String json) {
        if (json == null || json.isBlank()) {
            throw new ApplicationException("Import format missing format_details.");
        }
        try {
            return objectMapper.readValue(json, ImportFormatDetails.class);
        } catch (Exception ex) {
            throw new ApplicationException("Import format details JSON could not be parsed.");
        }
    }

    private ImportType resolveImportBean(String beanName) {
        if (beanName == null || beanName.isBlank()) {
            throw new ApplicationException("Import format missing bean_name.");
        }
        try {
            return applicationContext.getBean(beanName, ImportType.class);
        } catch (Exception ex) {
            throw new ApplicationException(String.format("Import type bean [%s] not found.", beanName));
        }
    }
}

