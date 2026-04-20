package zachkingcade.dev.ledger.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zachkingcade.dev.ledger.adapter.in.web.dto.ApiResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.MetaData;
import zachkingcade.dev.ledger.adapter.in.web.dto.pendingtransaction.GetAllPendingTransactionsResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.pendingtransaction.ImportPendingTransactionsResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.pendingtransaction.PendingTransactionObject;
import zachkingcade.dev.ledger.adapter.in.web.dto.pendingtransaction.RemovePendingTransactionResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.pendingtransaction.apply.ApplyPendingTransactionsFailureObject;
import zachkingcade.dev.ledger.adapter.in.web.dto.pendingtransaction.apply.ApplyPendingTransactionsRequest;
import zachkingcade.dev.ledger.adapter.in.web.dto.pendingtransaction.apply.ApplyPendingTransactionsResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.pendingtransaction.apply.ApplyPendingTransactionsSuccessObject;
import zachkingcade.dev.ledger.application.port.in.pendingtransaction.ImportPendingTransactionsUseCase;
import zachkingcade.dev.ledger.application.port.in.pendingtransaction.GetAllPendingTransactionsUseCase;
import zachkingcade.dev.ledger.application.port.in.pendingtransaction.RemovePendingTransactionUseCase;
import zachkingcade.dev.ledger.application.pendingtransaction.ImportPendingTransactionsResult;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.pendingtransaction.apply.ApplyPendingTransactionsCommand;
import zachkingcade.dev.ledger.application.pendingtransaction.apply.ApplyPendingTransactionsResult;
import zachkingcade.dev.ledger.application.port.in.pendingtransaction.ApplyPendingTransactionsUseCase;
import zachkingcade.dev.ledger.domain.pendingtransaction.PendingTransaction;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/pendingtransactions")
public class PendingTransactionController {

    private static final Logger log = LoggerFactory.getLogger(PendingTransactionController.class);

    private final GetAllPendingTransactionsUseCase getAllPendingTransactionsUseCase;
    private final RemovePendingTransactionUseCase removePendingTransactionUseCase;
    private final ImportPendingTransactionsUseCase importPendingTransactionsUseCase;
    private final ApplyPendingTransactionsUseCase applyPendingTransactionsUseCase;

    public PendingTransactionController(
            GetAllPendingTransactionsUseCase getAllPendingTransactionsUseCase,
            RemovePendingTransactionUseCase removePendingTransactionUseCase,
            ImportPendingTransactionsUseCase importPendingTransactionsUseCase,
            ApplyPendingTransactionsUseCase applyPendingTransactionsUseCase
    ) {
        this.getAllPendingTransactionsUseCase = getAllPendingTransactionsUseCase;
        this.removePendingTransactionUseCase = removePendingTransactionUseCase;
        this.importPendingTransactionsUseCase = importPendingTransactionsUseCase;
        this.applyPendingTransactionsUseCase = applyPendingTransactionsUseCase;
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<GetAllPendingTransactionsResponse>> getAll(@AuthenticationPrincipal Jwt jwt) {
        try {
            log.debug("Starting Rest Controller /pendingtransactions endpoint /all");
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            List<PendingTransaction> list = getAllPendingTransactionsUseCase.getAllPendingTransactionsForUser(userId);

            List<PendingTransactionObject> resulting = new ArrayList<>();
            for (PendingTransaction t : list) {
                resulting.add(new PendingTransactionObject(
                        t.transactionNumber(),
                        t.transactionDate().toString(),
                        t.description(),
                        t.amount(),
                        t.notes()
                ));
            }

            GetAllPendingTransactionsResponse response = new GetAllPendingTransactionsResponse(resulting);
            ApiResponse<GetAllPendingTransactionsResponse> apiResponse = new ApiResponse<>(
                    String.format("Returned [%s] Pending Transactions", resulting.size()),
                    new MetaData((long) resulting.size()),
                    response
            );
            log.debug("Ending Rest Controller /pendingtransactions endpoint /all count:[{}]", resulting.size());
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("PendingTransactionController.getAll failed", ex);
            throw ex;
        }
    }

    @DeleteMapping("/remove/{transactionNumber}")
    public ResponseEntity<ApiResponse<RemovePendingTransactionResponse>> remove(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long transactionNumber
    ) {
        try {
            log.debug("Starting Rest Controller /pendingtransactions endpoint /remove id:[{}]", transactionNumber);
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            removePendingTransactionUseCase.removePendingTransaction(userId, transactionNumber);

            RemovePendingTransactionResponse response = new RemovePendingTransactionResponse(transactionNumber);
            ApiResponse<RemovePendingTransactionResponse> apiResponse = new ApiResponse<>(
                    String.format("Removed Pending Transaction of ID:[%s]", transactionNumber),
                    new MetaData(0L),
                    response
            );
            log.debug("Ending Rest Controller /pendingtransactions endpoint /remove id:[{}]", transactionNumber);
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("PendingTransactionController.remove failed for id:[{}]", transactionNumber, ex);
            throw ex;
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImportPendingTransactionsResponse>> importFile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("formatId") Long formatId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            log.debug("Starting Rest Controller /pendingtransactions endpoint /import formatId:[{}] filePresent:[{}]", formatId, file != null);
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);

            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File is required.");
            }

            ImportPendingTransactionsResult result;
            try (InputStream is = file.getInputStream()) {
                result = importPendingTransactionsUseCase.importPendingTransactions(userId, formatId, is);
            }

            ImportPendingTransactionsResponse response = new ImportPendingTransactionsResponse(result.createdCount());
            ApiResponse<ImportPendingTransactionsResponse> apiResponse = new ApiResponse<>(
                    String.format("Imported [%s] Pending Transactions", result.createdCount()),
                    new MetaData((long) result.createdCount()),
                    response
            );
            log.debug("Ending Rest Controller /pendingtransactions endpoint /import createdCount:[{}]", result.createdCount());
            return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            log.error("PendingTransactionController.importFile failed for formatId:[{}]", formatId, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("PendingTransactionController.importFile failed for formatId:[{}]", formatId, ex);
            throw new ApplicationException("Unable to read uploaded file.");
        }
    }

    @PostMapping(value = "/apply", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ApplyPendingTransactionsResponse>> apply(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ApplyPendingTransactionsRequest request
    ) {
        try {
            log.debug("Starting Rest Controller /pendingtransactions endpoint /apply itemsCount:[{}]",
                    request == null || request.items() == null ? 0 : request.items().size()
            );
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);

            if (request == null || request.items() == null || request.items().isEmpty()) {
                throw new IllegalArgumentException("At least one item is required.");
            }

            List<ApplyPendingTransactionsCommand.Item> items = new ArrayList<>();
            for (var item : request.items()) {
                if (item == null) {
                    continue;
                }
                if (item.entryDate() == null || item.entryDate().isBlank()) {
                    throw new IllegalArgumentException("entryDate is required.");
                }
                LocalDate entryDate;
                try {
                    entryDate = LocalDate.parse(item.entryDate());
                } catch (Exception ex) {
                    throw new IllegalArgumentException(String.format("Invalid entryDate [%s].", item.entryDate()));
                }
                items.add(new ApplyPendingTransactionsCommand.Item(
                        item.pendingTransactionNumber(),
                        entryDate,
                        item.description(),
                        item.notes(),
                        item.journalLines()
                ));
            }

            ApplyPendingTransactionsResult result = applyPendingTransactionsUseCase.applyPendingTransactions(
                    new ApplyPendingTransactionsCommand(userId, items)
            );

            List<ApplyPendingTransactionsSuccessObject> succeeded = new ArrayList<>();
            for (var s : result.succeeded()) {
                succeeded.add(new ApplyPendingTransactionsSuccessObject(s.pendingTransactionNumber(), s.createdJournalEntryId()));
            }
            List<ApplyPendingTransactionsFailureObject> failed = new ArrayList<>();
            for (var f : result.failed()) {
                failed.add(new ApplyPendingTransactionsFailureObject(f.pendingTransactionNumber(), f.message()));
            }

            ApplyPendingTransactionsResponse response = new ApplyPendingTransactionsResponse(
                    result.successCount(),
                    result.failureCount(),
                    succeeded,
                    failed
            );

            ApiResponse<ApplyPendingTransactionsResponse> apiResponse = new ApiResponse<>(
                    String.format("Applied [%s] Pending Transactions with [%s] failures", result.successCount(), result.failureCount()),
                    new MetaData((long) result.successCount()),
                    response
            );
            log.debug("Ending Rest Controller /pendingtransactions endpoint /apply success:[{}] failed:[{}]", result.successCount(), result.failureCount());
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("PendingTransactionController.apply failed", ex);
            throw ex;
        }
    }
}

