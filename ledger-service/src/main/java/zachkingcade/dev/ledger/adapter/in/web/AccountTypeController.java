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
import zachkingcade.dev.ledger.adapter.in.web.dto.accounttype.*;
import zachkingcade.dev.ledger.application.commands.account.AccountFilterCommandObject;
import zachkingcade.dev.ledger.application.commands.accounttype.AccountTypeFilterCommandObject;
import zachkingcade.dev.ledger.application.commands.accounttype.CreateAccountTypeCommand;
import zachkingcade.dev.ledger.application.commands.accounttype.GetAllAccountTypesCommand;
import zachkingcade.dev.ledger.application.commands.accounttype.GetByIdAccountTypeCommand;
import zachkingcade.dev.ledger.application.commands.accounttype.UpdateAccountTypeCommand;
import zachkingcade.dev.ledger.application.commands.shared.SortObjectCommandObject;
import zachkingcade.dev.ledger.application.port.in.accounttype.CreateAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.in.accounttype.GetAllAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.in.accounttype.GetByIdAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.in.accounttype.UpdateAccountTypeUseCase;
import zachkingcade.dev.ledger.application.validation.AccountSortType;
import zachkingcade.dev.ledger.application.validation.AccountTypeSortType;
import zachkingcade.dev.ledger.application.validation.SortDirection;
import zachkingcade.dev.ledger.domain.account.AccountType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/accounttypes")
public class AccountTypeController {

    private static final Logger log = LoggerFactory.getLogger(AccountTypeController.class);

    private final CreateAccountTypeUseCase createAccountTypeUseCase;
    private final GetAllAccountTypeUseCase getAllAccountTypeUseCase;
    private final GetByIdAccountTypeUseCase getByIdAccountTypeUseCase;
    private final UpdateAccountTypeUseCase updateAccountTypeUseCase;

    public AccountTypeController(CreateAccountTypeUseCase createAccountTypeUseCase, GetAllAccountTypeUseCase getAllAccountTypeUseCase, GetByIdAccountTypeUseCase getByIdAccountTypeUseCase, UpdateAccountTypeUseCase updateAccountTypeUseCase) {
        this.createAccountTypeUseCase = createAccountTypeUseCase;
        this.getAllAccountTypeUseCase = getAllAccountTypeUseCase;
        this.getByIdAccountTypeUseCase = getByIdAccountTypeUseCase;
        this.updateAccountTypeUseCase = updateAccountTypeUseCase;
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<GetAllAccountTypesResponse>> getAllAccountTypes(@AuthenticationPrincipal Jwt jwt){
        try {
            log.debug("Starting Rest Controller /accounttypes endpoint /all");
            return handleGetAll(jwt, null);
        } catch (RuntimeException ex) {
            log.error("AccountTypeController.getAllAccountTypes failed", ex);
            throw ex;
        }
    }

    @PostMapping("/all/filtered")
    public ResponseEntity<ApiResponse<GetAllAccountTypesResponse>> getAllAccountTypesFiltered(@AuthenticationPrincipal Jwt jwt, @RequestBody(required = false) GetAllAccountTypesRequest request){
        try {
            log.debug("Starting Rest Controller /accounttypes endpoint /all/filtered sortPresent:[{}] filtersPresent:[{}]",
                    request != null && request.sort().isPresent(),
                    request != null && request.filters().isPresent()
            );
            ResponseEntity<ApiResponse<GetAllAccountTypesResponse>> result = handleGetAll(jwt, request);
            Long count = null;
            if (result.getBody() != null && result.getBody().getMetaData() != null) {
                count = result.getBody().getMetaData().getDataResponseCount();
            }
            log.debug("Ending Rest Controller /accounttypes endpoint /all/filtered count:[{}]", count);
            return result;
        } catch (RuntimeException ex) {
            log.error("AccountTypeController.getAllAccountTypesFiltered failed", ex);
            throw ex;
        }
    }

    private ResponseEntity<ApiResponse<GetAllAccountTypesResponse>> handleGetAll(Jwt jwt, GetAllAccountTypesRequest request){
        try {
            log.debug("Starting Rest Controller /accounttypes endpoint /all requestPresent:[{}]", request != null);
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            // Sanitize Request
            SortObjectCommandObject<AccountTypeSortType> sort = null;
            if(request != null && request.sort().isPresent()){
                sort = new SortObjectCommandObject<>(request.sort().get().type(), request.sort().get().direction() != null? request.sort().get().direction() : SortDirection.ascending );
            } else {
                //default
                sort = new SortObjectCommandObject<>(AccountTypeSortType.id, SortDirection.ascending);
            }

            // Sanitize Request Filters
            AccountTypeFilterCommandObject filters = null;
            if(request != null && request.filters().isPresent()){
                filters = new AccountTypeFilterCommandObject(
                        request.filters().get().descriptionContains(),
                        request.filters().get().notesContains(),
                        request.filters().get().accountClass(),
                        request.filters().get().hideInactive(),
                        request.filters().get().hideActive(),
                        request.filters().get().searchContains(),
                        request.filters().get().hideSystemAccounts()
                );
            } else {
                //default
                filters = new AccountTypeFilterCommandObject(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(false), Optional.of(false), Optional.empty(), Optional.empty());
            }

            GetAllAccountTypesCommand command = new GetAllAccountTypesCommand(userId, Optional.of(sort), Optional.of(filters));
            List<AccountType> list = getAllAccountTypeUseCase.getAllAccountTypes(command);

            List<AccountTypeObject> resultingList = new ArrayList<>();
            for(AccountType accountType: list){
                resultingList.add(new AccountTypeObject(
                        accountType.id(),
                        accountType.classificationId(),
                        accountType.description(),
                        accountType.active(),
                        accountType.notes(),
                        Boolean.TRUE.equals(accountType.getSystemAccount())
                ));
            }

            GetAllAccountTypesResponse response = new GetAllAccountTypesResponse(resultingList);
            ApiResponse<GetAllAccountTypesResponse> apiResponse = new ApiResponse<>(String.format("Returned [%s] Account Types", resultingList.size()),new MetaData((long) resultingList.size()),response);
            log.debug("Ending Rest Controller /accounttypes endpoint /all with [{}] results",resultingList.size());
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("AccountTypeController.handleGetAll failed", ex);
            throw ex;
        }
    }

    @GetMapping("/byid/{id}")
    public ResponseEntity<ApiResponse<GetAccountTypeByIdResponse>> getAccountTypeById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id){
        try {
            log.debug("Starting Rest Controller /accounttypes endpoint /byid id:[{}]",id);
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            AccountType accountType = getByIdAccountTypeUseCase.getAccountTypeById(new GetByIdAccountTypeCommand(userId, id));
            GetAccountTypeByIdResponse response = new GetAccountTypeByIdResponse(
                    accountType.id(),
                    accountType.classificationId(),
                    accountType.description(),
                    accountType.active(),
                    accountType.notes(),
                    Boolean.TRUE.equals(accountType.getSystemAccount())
            );
            ApiResponse<GetAccountTypeByIdResponse> apiResponse = new ApiResponse<>(String.format("Returned Account Type of ID:[%s]", id),new MetaData(1L),response);
            log.debug("Ending Rest Controller /accounttypes endpoint /byid id:[{}]",response.id());
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("AccountTypeController.getAccountTypeById failed for id:[{}]", id, ex);
            throw ex;
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CreateAccountTypeResponse>> createAccountType(@AuthenticationPrincipal Jwt jwt, @RequestBody CreateAccountTypeRequest request){
        try {
            log.debug("Starting Rest Controller /accounttypes endpoint /add classificationId:[{}] description:[{}]",request.classificationId(),request.description());
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            CreateAccountTypeCommand command = new CreateAccountTypeCommand(userId, request.classificationId(), request.description(), request.notes());
            AccountType accountType = createAccountTypeUseCase.createAccountType(command);
            CreateAccountTypeResponse response = new CreateAccountTypeResponse(
                    accountType.id(),
                    accountType.classificationId(),
                    accountType.description(),
                    accountType.active(),
                    accountType.notes(),
                    Boolean.TRUE.equals(accountType.getSystemAccount())
            );
            ApiResponse<CreateAccountTypeResponse> apiResponse = new ApiResponse<>(String.format("Created Account Type [%s]", request.description()),new MetaData(1L),response);
            log.debug("Ending Rest Controller /accounttypes endpoint /add createdId:[{}]",response.id());
            return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            log.error("AccountTypeController.createAccountType failed for request:[{}]", request, ex);
            throw ex;
        }
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<UpdateAccountTypeResponse>> updateAccountType(@AuthenticationPrincipal Jwt jwt, @RequestBody UpdateAccountTypeRequest request){
        try {
            log.debug("Starting Rest Controller /accounttypes endpoint /update id:[{}] descriptiont:[{}]",request.id(),request.description());
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            UpdateAccountTypeCommand command = new UpdateAccountTypeCommand(userId, request.id(), request.description(),request.notes(),request.active());
            AccountType accountType = updateAccountTypeUseCase.updateAccountType(command);
            UpdateAccountTypeResponse response = new UpdateAccountTypeResponse(
                    accountType.id(),
                    accountType.classificationId(),
                    accountType.description(),
                    accountType.active(),
                    accountType.notes(),
                    Boolean.TRUE.equals(accountType.getSystemAccount())
            );
            ApiResponse<UpdateAccountTypeResponse> apiResponse = new ApiResponse<>(String.format("Updated Account Type of ID:[%s]", request.id()),new MetaData(1L),response);
            log.debug("Ending Rest Controller /accounttypes endpoint /update updatedId:[{}]",response.id());
            return new ResponseEntity<>(apiResponse,HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("AccountTypeController.updateAccountType failed for request:[{}]", request, ex);
            throw ex;
        }
    }

}
