package zachkingcade.dev.ledger.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zachkingcade.dev.ledger.adapter.in.web.dto.ApiResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.MetaData;
import zachkingcade.dev.ledger.adapter.in.web.dto.account.*;
import zachkingcade.dev.ledger.adapter.in.web.dto.accountclassifcation.GetAllAccountClassificationResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.shared.SortObject;
import zachkingcade.dev.ledger.application.commands.account.CreateAccountCommand;
import zachkingcade.dev.ledger.application.commands.account.GetAllAccountCommand;
import zachkingcade.dev.ledger.application.commands.account.UpdateAccountCommand;
import zachkingcade.dev.ledger.application.commands.shared.SortObjectCommandObject;
import zachkingcade.dev.ledger.application.port.in.account.CreateAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.account.GetByIdAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.account.GetAllAccountsUseCase;
import zachkingcade.dev.ledger.application.port.in.account.UpdateAccountUseCase;
import zachkingcade.dev.ledger.application.validation.AccountSortType;
import zachkingcade.dev.ledger.application.validation.SortDirection;
import zachkingcade.dev.ledger.domain.account.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final GetAllAccountsUseCase getallAccountsUseCase;
    private final GetByIdAccountUseCase getByIdAccountUseCase;
    private final CreateAccountUseCase createAccountUseCase;
    private final UpdateAccountUseCase updateAccountUseCase;

    public AccountController(GetAllAccountsUseCase getallAccountsUseCase, GetByIdAccountUseCase getByIdAccountUseCase, CreateAccountUseCase createAccountUseCase, UpdateAccountUseCase updateAccountUseCase) {
        this.getallAccountsUseCase = getallAccountsUseCase;
        this.getByIdAccountUseCase = getByIdAccountUseCase;
        this.createAccountUseCase = createAccountUseCase;
        this.updateAccountUseCase = updateAccountUseCase;
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<GetAllAccountsResponse>> getAll(@RequestBody(required = false) GetAllAccountsRequest request){
        try {
            log.debug("Starting Rest Controller /accounts endpoint /all request[{}]", request);
            // Sanitize Request
            SortObjectCommandObject<AccountSortType> sort = null;
            if(request != null && request.sort().isPresent()){
                sort = new SortObjectCommandObject<>(request.sort().get().type(), request.sort().get().direction() != null? request.sort().get().direction() : SortDirection.ascending );
            } else {
                //default
                sort = new SortObjectCommandObject<>(AccountSortType.id, SortDirection.ascending);
            }

            GetAllAccountCommand command = new GetAllAccountCommand(Optional.of(sort));
            List<Account> domainList = getallAccountsUseCase.getAllAccounts(command);
            List<AccountObject> resultingList = new ArrayList<>();
            for(Account account: domainList){
                resultingList.add(new AccountObject(account.id(), account.typeId(), account.description(),account.active(),account.notes()));
            }
            GetAllAccountsResponse response = new GetAllAccountsResponse(resultingList);
            ApiResponse<GetAllAccountsResponse> apiResponse = new ApiResponse<>(String.format("Returned [%s] Accounts", resultingList.size()),new MetaData((long) resultingList.size()),response);
            log.debug("Ending Rest Controller /accounts endpoint /all with [{}] results",resultingList.size());
            return new ResponseEntity< >(apiResponse,HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("AccountController.getAll failed", ex);
            throw ex;
        }
    }

    @GetMapping("/byid/{id}")
    public ResponseEntity<ApiResponse<GetAccountByIdResponse>> getById(@PathVariable Long id){
        try {
            log.debug("Starting Rest Controller /accounts endpoint /byid id:[{}]",id);
            Account foundAccount = getByIdAccountUseCase.getAccountById(id);
            GetAccountByIdResponse response = new GetAccountByIdResponse(foundAccount.id(), foundAccount.typeId(), foundAccount.description(), foundAccount.active(), foundAccount.notes());
            ApiResponse<GetAccountByIdResponse> apiResponse = new ApiResponse<>(String.format("Returned Account of ID:[%s]", id),new MetaData(1L),response);
            log.debug("Ending Rest Controller /accounts endpoint /byid id:[{}]",response.accountId());
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("AccountController.getById failed for id:[{}]", id, ex);
            throw ex;
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CreateAccountResponse>> create(@RequestBody CreateAccountRequest request) {
        try {
            log.debug("Starting Rest Controller /accounts endpoint /add typeId:[{}] description:[{}]",request.typeId(),request.description());
            CreateAccountCommand command = new CreateAccountCommand(request.typeId(), request.description(), request.notes());
            Account result = createAccountUseCase.createAccount(command);
            CreateAccountResponse response = new CreateAccountResponse(result.id(), result.typeId(), result.description(), result.active(), result.notes());
            ApiResponse<CreateAccountResponse> apiResponse = new ApiResponse<>(String.format("Created Account [%s]", request.description()),new MetaData(1L),response);
            log.debug("Ending Rest Controller /accounts endpoint /add createdId:[{}]",response.accountId());
            return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            log.error("AccountController.create failed for request:[{}]", request, ex);
            throw ex;
        }
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<UpdateAccountResponse>> updateAccount(@RequestBody UpdateAccountRequest request){
        try {
            log.debug("Starting Rest Controller /accounts endpoint /update id:[{}] description:[{}]",request.id(),request.description());
            UpdateAccountCommand command = new UpdateAccountCommand(request.id(), request.description(), request.notes(), request.active());
            Account result = updateAccountUseCase.updateAccount(command);
            UpdateAccountResponse response = new UpdateAccountResponse(request.id(), result.typeId(), result.description(), result.active(), result.notes());
            ApiResponse<UpdateAccountResponse> apiResponse = new ApiResponse<>(String.format("Updated Account of ID:[%s]", request.id()),new MetaData(1L),response);
            log.debug("Ending Rest Controller /accounts endpoint /update updatedId:[{}]",response.accountId());
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("AccountController.updateAccount failed for request:[{}]", request, ex);
            throw ex;
        }
    }
}
