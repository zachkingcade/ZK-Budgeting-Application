package zachkingcade.dev.ledger.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zachkingcade.dev.ledger.adapter.in.web.dto.ApiResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.MetaData;
import zachkingcade.dev.ledger.adapter.in.web.dto.accountingperiod.CloseAccountingPeriodRequest;
import zachkingcade.dev.ledger.application.accountingperiod.AccountingPeriodViews.ArchivedJournalEntryView;
import zachkingcade.dev.ledger.application.accountingperiod.AccountingPeriodViews.ClosedPeriodAccountBalanceView;
import zachkingcade.dev.ledger.application.accountingperiod.AccountingPeriodViews.ClosedPeriodListItem;
import zachkingcade.dev.ledger.application.accountingperiod.AccountingPeriodViews.NextToCloseView;
import zachkingcade.dev.ledger.application.port.in.accountingperiod.AccountingPeriodUseCase;

import java.util.List;

@RestController
@RequestMapping("/accounting-periods")
public class AccountingPeriodController {

    private final AccountingPeriodUseCase accountingPeriodUseCase;

    public AccountingPeriodController(AccountingPeriodUseCase accountingPeriodUseCase) {
        this.accountingPeriodUseCase = accountingPeriodUseCase;
    }

    @GetMapping("/closed")
    public ResponseEntity<ApiResponse<List<ClosedPeriodListItem>>> listClosed(@AuthenticationPrincipal Jwt jwt) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        List<ClosedPeriodListItem> items = accountingPeriodUseCase.listClosed(userId);
        return ResponseEntity.ok(new ApiResponse<>("Listed closed accounting periods", new MetaData((long) items.size()), items));
    }

    @GetMapping("/closed/{closedPeriodId}/journal-entries")
    public ResponseEntity<ApiResponse<List<ArchivedJournalEntryView>>> listArchivedEntries(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long closedPeriodId) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        List<ArchivedJournalEntryView> entries = accountingPeriodUseCase.listArchivedJournalEntries(userId, closedPeriodId);
        return ResponseEntity.ok(new ApiResponse<>("Listed archived journal entries", new MetaData((long) entries.size()), entries));
    }

    @GetMapping("/closed/{closedPeriodId}/account-balances")
    public ResponseEntity<ApiResponse<List<ClosedPeriodAccountBalanceView>>> listAccountBalances(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long closedPeriodId) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        List<ClosedPeriodAccountBalanceView> balances =
                accountingPeriodUseCase.listAccountBalancesForClosedPeriod(userId, closedPeriodId);
        return ResponseEntity.ok(new ApiResponse<>("Listed period-end account balances", new MetaData((long) balances.size()), balances));
    }

    @GetMapping("/next-to-close")
    public ResponseEntity<ApiResponse<NextToCloseView>> nextToClose(@AuthenticationPrincipal Jwt jwt) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        NextToCloseView view = accountingPeriodUseCase.getNextToClose(userId);
        return ResponseEntity.ok(new ApiResponse<>("Next accounting period to close", new MetaData(1L), view));
    }

    @PostMapping("/close")
    public ResponseEntity<ApiResponse<ClosedPeriodListItem>> close(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CloseAccountingPeriodRequest request) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        ClosedPeriodListItem closed = accountingPeriodUseCase.closePeriod(userId, request.startDate(), request.endDate());
        return ResponseEntity.ok(new ApiResponse<>("Closed accounting period", new MetaData(1L), closed));
    }
}
