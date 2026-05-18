package zachkingcade.dev.ledger.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import zachkingcade.dev.ledger.adapter.in.web.dto.ApiResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.MetaData;
import zachkingcade.dev.ledger.adapter.in.web.dto.budgetplan.request.BudgetPlanRequests.*;
import zachkingcade.dev.ledger.application.budgetplan.BudgetPlanningViews.*;
import zachkingcade.dev.ledger.application.port.in.budgetplan.BudgetPlanningUseCase;

import java.util.List;

@RestController
@RequestMapping("/budget-plans")
public class BudgetPlanController {

    private static final Logger log = LoggerFactory.getLogger(BudgetPlanController.class);

    private final BudgetPlanningUseCase budgetPlanningUseCase;

    public BudgetPlanController(BudgetPlanningUseCase budgetPlanningUseCase) {
        this.budgetPlanningUseCase = budgetPlanningUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetPlanListItem>>> list(@AuthenticationPrincipal Jwt jwt) {
        try {
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            List<BudgetPlanListItem> plans = budgetPlanningUseCase.listPlans(userId);
            MetaData meta = new MetaData((long) plans.size());
            return ResponseEntity.ok(new ApiResponse<>("Listed budget plans", meta, plans));
        } catch (RuntimeException ex) {
            log.error("BudgetPlanController.list failed", ex);
            throw ex;
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetPlanListItem>> create(
            @AuthenticationPrincipal Jwt jwt, @RequestBody CreateBudgetPlanRequest request) {
        try {
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            BudgetPlanListItem created = budgetPlanningUseCase.createPlan(userId, request.name());
            return new ResponseEntity<>(
                    new ApiResponse<>("Created budget plan", new MetaData(1L), created), HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            log.error("BudgetPlanController.create failed", ex);
            throw ex;
        }
    }

    @PatchMapping("/{planId}")
    public ResponseEntity<ApiResponse<BudgetPlanListItem>> rename(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long planId,
            @RequestBody RenameBudgetPlanRequest request) {
        try {
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            BudgetPlanListItem updated = budgetPlanningUseCase.renamePlan(userId, planId, request.name());
            return ResponseEntity.ok(new ApiResponse<>("Renamed budget plan", new MetaData(1L), updated));
        } catch (RuntimeException ex) {
            log.error("BudgetPlanController.rename failed planId:[{}]", planId, ex);
            throw ex;
        }
    }

    @PostMapping("/{planId}/duplicate")
    public ResponseEntity<ApiResponse<BudgetPlanListItem>> duplicate(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long planId,
            @RequestBody DuplicateBudgetPlanRequest request) {
        try {
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            BudgetPlanListItem created = budgetPlanningUseCase.duplicatePlan(
                    userId,
                    planId,
                    request.name(),
                    request.copyIncomes(),
                    request.copyRecurringPayables(),
                    request.copyBudgetAmounts());
            return new ResponseEntity<>(
                    new ApiResponse<>("Duplicated budget plan", new MetaData(1L), created), HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            log.error("BudgetPlanController.duplicate failed planId:[{}]", planId, ex);
            throw ex;
        }
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long planId) {
        try {
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            budgetPlanningUseCase.deletePlan(userId, planId);
            return ResponseEntity.ok(new ApiResponse<>("Deleted budget plan", new MetaData(0L), null));
        } catch (RuntimeException ex) {
            log.error("BudgetPlanController.delete failed planId:[{}]", planId, ex);
            throw ex;
        }
    }

    @GetMapping("/{planId}/editor")
    public ResponseEntity<ApiResponse<BudgetPlanEditorView>> editor(@AuthenticationPrincipal Jwt jwt, @PathVariable Long planId) {
        try {
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            BudgetPlanEditorView data = budgetPlanningUseCase.loadEditor(userId, planId);
            return ResponseEntity.ok(new ApiResponse<>("Loaded budget plan editor", new MetaData(1L), data));
        } catch (RuntimeException ex) {
            log.error("BudgetPlanController.editor failed planId:[{}]", planId, ex);
            throw ex;
        }
    }

    @GetMapping("/{planId}/incomes")
    public ResponseEntity<ApiResponse<List<BpIncomeResponse>>> listIncomes(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long planId) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        List<BpIncomeResponse> list = budgetPlanningUseCase.listIncomes(userId, planId);
        return ResponseEntity.ok(new ApiResponse<>("Listed incomes", new MetaData((long) list.size()), list));
    }

    @PostMapping("/{planId}/incomes")
    public ResponseEntity<ApiResponse<BpIncomeResponse>> createIncome(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long planId, @RequestBody CreateBpIncomeRequest request) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        BpIncomeResponse created = budgetPlanningUseCase.createIncome(
                userId, planId, request.amountMinor(), request.frequencyUnit(), request.frequencyNumber(), request.accountId());
        return new ResponseEntity<>(
                new ApiResponse<>("Created income", new MetaData(1L), created), HttpStatus.CREATED);
    }

    @PatchMapping("/{planId}/incomes/{incomeId}")
    public ResponseEntity<ApiResponse<BpIncomeResponse>> updateIncome(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long planId,
            @PathVariable Long incomeId,
            @RequestBody UpdateBpIncomeRequest request) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        BpIncomeResponse updated = budgetPlanningUseCase.updateIncome(
                userId,
                planId,
                incomeId,
                request.amountMinor(),
                request.frequencyUnit(),
                request.frequencyNumber(),
                request.accountId());
        return ResponseEntity.ok(new ApiResponse<>("Updated income", new MetaData(1L), updated));
    }

    @DeleteMapping("/{planId}/incomes/{incomeId}")
    public ResponseEntity<ApiResponse<Void>> deleteIncome(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long planId, @PathVariable Long incomeId) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        budgetPlanningUseCase.deleteIncome(userId, planId, incomeId);
        return ResponseEntity.ok(new ApiResponse<>("Deleted income", new MetaData(0L), null));
    }

    @GetMapping("/{planId}/recurring-payables")
    public ResponseEntity<ApiResponse<List<BpRecurringPayableResponse>>> listPayables(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long planId) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        List<BpRecurringPayableResponse> list = budgetPlanningUseCase.listRecurringPayables(userId, planId);
        return ResponseEntity.ok(new ApiResponse<>("Listed recurring payables", new MetaData((long) list.size()), list));
    }

    @PostMapping("/{planId}/recurring-payables")
    public ResponseEntity<ApiResponse<BpRecurringPayableResponse>> createPayable(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long planId,
            @RequestBody CreateBpRecurringPayableRequest request) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        BpRecurringPayableResponse created = budgetPlanningUseCase.createRecurringPayable(
                userId,
                planId,
                request.description(),
                request.amountMinor(),
                request.frequencyUnit(),
                request.frequencyNumber(),
                request.budgetAccountId());
        return new ResponseEntity<>(
                new ApiResponse<>("Created recurring payable", new MetaData(1L), created), HttpStatus.CREATED);
    }

    @PatchMapping("/{planId}/recurring-payables/{payableId}")
    public ResponseEntity<ApiResponse<BpRecurringPayableResponse>> updatePayable(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long planId,
            @PathVariable Long payableId,
            @RequestBody UpdateBpRecurringPayableRequest request) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        BpRecurringPayableResponse updated = budgetPlanningUseCase.updateRecurringPayable(
                userId,
                planId,
                payableId,
                request.description(),
                request.amountMinor(),
                request.frequencyUnit(),
                request.frequencyNumber(),
                request.budgetAccountId());
        return ResponseEntity.ok(new ApiResponse<>("Updated recurring payable", new MetaData(1L), updated));
    }

    @DeleteMapping("/{planId}/recurring-payables/{payableId}")
    public ResponseEntity<ApiResponse<Void>> deletePayable(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long planId, @PathVariable Long payableId) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        budgetPlanningUseCase.deleteRecurringPayable(userId, planId, payableId);
        return ResponseEntity.ok(new ApiResponse<>("Deleted recurring payable", new MetaData(0L), null));
    }

    @GetMapping("/{planId}/budgets")
    public ResponseEntity<ApiResponse<List<BpBudgetResponse>>> listBudgets(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long planId) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        List<BpBudgetResponse> list = budgetPlanningUseCase.listBudgets(userId, planId);
        return ResponseEntity.ok(new ApiResponse<>("Listed budgets", new MetaData((long) list.size()), list));
    }

    @PostMapping("/{planId}/budgets")
    public ResponseEntity<ApiResponse<BpBudgetResponse>> createBudget(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long planId, @RequestBody CreateBpBudgetRequest request) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        BpBudgetResponse created = budgetPlanningUseCase.createBudget(
                userId,
                planId,
                request.amountMinor(),
                request.frequencyUnit(),
                request.frequencyNumber(),
                request.accountId());
        return new ResponseEntity<>(
                new ApiResponse<>("Created budget", new MetaData(1L), created), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{planId}/budgets/{budgetId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<BpBudgetResponse>> updateBudget(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long planId,
            @PathVariable Long budgetId,
            @RequestBody UpdateBpBudgetRequest request) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        BpBudgetResponse updated = budgetPlanningUseCase.updateBudget(
                userId,
                planId,
                budgetId,
                request.amountMinor(),
                request.frequencyUnit(),
                request.frequencyNumber(),
                request.accountId());
        return ResponseEntity.ok(new ApiResponse<>("Updated budget", new MetaData(1L), updated));
    }

    @DeleteMapping("/{planId}/budgets/{budgetId}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long planId, @PathVariable Long budgetId) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        budgetPlanningUseCase.deleteBudget(userId, planId, budgetId);
        return ResponseEntity.ok(new ApiResponse<>("Deleted budget", new MetaData(0L), null));
    }

    @GetMapping("/{planId}/budgets/{budgetId}/funding-lines")
    public ResponseEntity<ApiResponse<List<BpFundingLineResponse>>> listFundingLines(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long planId, @PathVariable Long budgetId) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        List<BpFundingLineResponse> list = budgetPlanningUseCase.listFundingLines(userId, planId, budgetId);
        return ResponseEntity.ok(new ApiResponse<>("Listed funding lines", new MetaData((long) list.size()), list));
    }

    @PostMapping("/{planId}/budgets/{budgetId}/funding-lines")
    public ResponseEntity<ApiResponse<BpFundingLineResponse>> createFundingLine(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long planId,
            @PathVariable Long budgetId,
            @RequestBody CreateBpFundingLineRequest request) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        BpFundingLineResponse created = budgetPlanningUseCase.createFundingLine(
                userId,
                planId,
                budgetId,
                request.bpIncomeId(),
                request.amountMinor(),
                request.frequencyUnit(),
                request.frequencyNumber());
        return new ResponseEntity<>(
                new ApiResponse<>("Created funding line", new MetaData(1L), created), HttpStatus.CREATED);
    }

    @PatchMapping("/{planId}/budgets/{budgetId}/funding-lines/{lineId}")
    public ResponseEntity<ApiResponse<BpFundingLineResponse>> updateFundingLine(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long planId,
            @PathVariable Long budgetId,
            @PathVariable Long lineId,
            @RequestBody UpdateBpFundingLineRequest request) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        BpFundingLineResponse updated = budgetPlanningUseCase.updateFundingLine(
                userId,
                planId,
                budgetId,
                lineId,
                request.bpIncomeId(),
                request.amountMinor(),
                request.frequencyUnit(),
                request.frequencyNumber());
        return ResponseEntity.ok(new ApiResponse<>("Updated funding line", new MetaData(1L), updated));
    }

    @DeleteMapping("/{planId}/budgets/{budgetId}/funding-lines/{lineId}")
    public ResponseEntity<ApiResponse<Void>> deleteFundingLine(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long planId,
            @PathVariable Long budgetId,
            @PathVariable Long lineId) {
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
        budgetPlanningUseCase.deleteFundingLine(userId, planId, budgetId, lineId);
        return ResponseEntity.ok(new ApiResponse<>("Deleted funding line", new MetaData(0L), null));
    }
}
