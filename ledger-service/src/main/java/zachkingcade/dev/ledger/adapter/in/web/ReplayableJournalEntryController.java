package zachkingcade.dev.ledger.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import zachkingcade.dev.ledger.adapter.in.web.dto.ApiResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.MetaData;
import zachkingcade.dev.ledger.adapter.in.web.dto.replayablejournal.ReplayableJournalEntryRequests.ReplayableJournalEntryLineRequest;
import zachkingcade.dev.ledger.adapter.in.web.dto.replayablejournal.ReplayableJournalEntryRequests.SaveReplayableJournalEntryRequest;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.port.in.replayablejournal.ReplayableJournalEntryUseCase;
import zachkingcade.dev.ledger.application.replayablejournal.ReplayableJournalEntryViews.ReplayableJournalEntryDetailView;
import zachkingcade.dev.ledger.application.replayablejournal.ReplayableJournalEntryViews.ReplayableJournalEntryListItem;
import zachkingcade.dev.ledger.application.replayablejournal.ReplayableJournalEntryViews.ReplayableJournalEntryLineView;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/replayable-journal-entries")
public class ReplayableJournalEntryController {

    private static final Logger log = LoggerFactory.getLogger(ReplayableJournalEntryController.class);

    private final ReplayableJournalEntryUseCase replayableJournalEntryUseCase;

    public ReplayableJournalEntryController(ReplayableJournalEntryUseCase replayableJournalEntryUseCase) {
        this.replayableJournalEntryUseCase = replayableJournalEntryUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReplayableJournalEntryListItem>>> list(@AuthenticationPrincipal Jwt jwt) {
        try {
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            List<ReplayableJournalEntryListItem> items = replayableJournalEntryUseCase.list(userId);
            return ResponseEntity.ok(
                    new ApiResponse<>("Listed replayable journal entries", new MetaData((long) items.size()), items));
        } catch (RuntimeException ex) {
            log.error("ReplayableJournalEntryController.list failed", ex);
            throw ex;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReplayableJournalEntryDetailView>> getById(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        try {
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            ReplayableJournalEntryDetailView detail = replayableJournalEntryUseCase.getById(userId, id);
            return ResponseEntity.ok(
                    new ApiResponse<>("Loaded replayable journal entry", new MetaData(1L), detail));
        } catch (RuntimeException ex) {
            log.error("ReplayableJournalEntryController.getById failed id:[{}]", id, ex);
            throw ex;
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReplayableJournalEntryDetailView>> create(
            @AuthenticationPrincipal Jwt jwt, @RequestBody SaveReplayableJournalEntryRequest request) {
        try {
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            boolean requireBalanced = request.requireBalanced() == null || request.requireBalanced();
            ReplayableJournalEntryDetailView created = replayableJournalEntryUseCase.create(
                    userId, request.replayName(), mapLines(request.lines()), requireBalanced);
            return new ResponseEntity<>(
                    new ApiResponse<>("Created replayable journal entry", new MetaData(1L), created),
                    HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            log.error("ReplayableJournalEntryController.create failed", ex);
            throw ex;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReplayableJournalEntryDetailView>> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody SaveReplayableJournalEntryRequest request) {
        try {
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            boolean requireBalanced = request.requireBalanced() == null || request.requireBalanced();
            ReplayableJournalEntryDetailView updated = replayableJournalEntryUseCase.update(
                    userId, id, request.replayName(), mapLines(request.lines()), requireBalanced);
            return ResponseEntity.ok(
                    new ApiResponse<>("Updated replayable journal entry", new MetaData(1L), updated));
        } catch (RuntimeException ex) {
            log.error("ReplayableJournalEntryController.update failed id:[{}]", id, ex);
            throw ex;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        try {
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            replayableJournalEntryUseCase.delete(userId, id);
            return ResponseEntity.ok(new ApiResponse<>("Deleted replayable journal entry", new MetaData(0L), null));
        } catch (RuntimeException ex) {
            log.error("ReplayableJournalEntryController.delete failed id:[{}]", id, ex);
            throw ex;
        }
    }

    private static List<ReplayableJournalEntryLineView> mapLines(List<ReplayableJournalEntryLineRequest> lines) {
        if (lines == null) {
            throw new ApplicationException("At least two lines are required.");
        }
        List<ReplayableJournalEntryLineView> result = new ArrayList<>();
        for (ReplayableJournalEntryLineRequest line : lines) {
            if (line == null) {
                continue;
            }
            if (line.amount() == null || line.amount() <= 0) {
                throw new ApplicationException("Each line requires a positive amount.");
            }
            if (line.accountId() == null || line.accountId() <= 0) {
                throw new ApplicationException("Each line requires an account.");
            }
            if (line.direction() == null
                    || (line.direction() != 'C' && line.direction() != 'D' && line.direction() != 'c' && line.direction() != 'd')) {
                throw new ApplicationException("Each line direction must be C or D.");
            }
            char direction = Character.toUpperCase(line.direction());
            result.add(new ReplayableJournalEntryLineView(line.amount(), line.accountId(), direction));
        }
        return result;
    }
}
