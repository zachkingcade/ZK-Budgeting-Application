package zachkingcade.dev.ledger.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zachkingcade.dev.ledger.adapter.in.web.dto.ApiResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.MetaData;
import zachkingcade.dev.ledger.adapter.in.web.dto.accountclassifcation.GetAllAccountClassificationResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.accounttype.*;
import zachkingcade.dev.ledger.application.commands.accounttype.CreateAccountTypeCommand;
import zachkingcade.dev.ledger.application.commands.accounttype.UpdateAccountTypeCommand;
import zachkingcade.dev.ledger.application.port.in.accounttype.CreateAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.in.accounttype.GetAllAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.in.accounttype.GetByIdAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.in.accounttype.UpdateAccountTypeUseCase;
import zachkingcade.dev.ledger.domain.account.AccountType;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/accounttypes")
public class AccountTypeController {

    private static final Logger log = LoggerFactory.getLogger(AccountTypeController.class);

    private final CreateAccountTypeUseCase createAccountTypeUseCase;
    private final GetAllAccountTypeUseCase getallAccountTypeUseCase;
    private final GetByIdAccountTypeUseCase getbyidAccountTypeUseCase;
    private final UpdateAccountTypeUseCase updateAccountTypeUseCase;

    public AccountTypeController(CreateAccountTypeUseCase createAccountTypeUseCase, GetAllAccountTypeUseCase getallAccountTypeUseCase, GetByIdAccountTypeUseCase getbyidAccountTypeUseCase, UpdateAccountTypeUseCase updateAccountTypeUseCase) {
        this.createAccountTypeUseCase = createAccountTypeUseCase;
        this.getallAccountTypeUseCase = getallAccountTypeUseCase;
        this.getbyidAccountTypeUseCase = getbyidAccountTypeUseCase;
        this.updateAccountTypeUseCase = updateAccountTypeUseCase;
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<GetAllAccountTypesResponse>> getAllAccountTypes(){
        try {
            log.debug("Starting Rest Controller /accounttypes endpoint /all");
            List<AccountType> list = getallAccountTypeUseCase.getAllAccountTypes();

            List<AccountTypeObject> resultingList = new ArrayList<>();
            for(AccountType accountType: list){
                resultingList.add(new AccountTypeObject(accountType.id(),accountType.classificationId(),accountType.description(),accountType.active(),accountType.notes()));
            }

            GetAllAccountTypesResponse response = new GetAllAccountTypesResponse(resultingList);
            ApiResponse<GetAllAccountTypesResponse> apiResponse = new ApiResponse<>(String.format("Returned [%s] Account Types", resultingList.size()),new MetaData((long) resultingList.size()),response);
            log.debug("Ending Rest Controller /accounttypes endpoint /all with [{}] results",resultingList.size());
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("AccountTypeController.getAllAccountTypes failed", ex);
            throw ex;
        }
    }

    @GetMapping("/byid/{id}")
    public ResponseEntity<ApiResponse<GetAccountTypeByIdResponse>> getAccountTypeById(@PathVariable Long id){
        try {
            log.debug("Starting Rest Controller /accounttypes endpoint /byid id:[{}]",id);
            AccountType accountType = getbyidAccountTypeUseCase.getAccountTypeById(id);
            GetAccountTypeByIdResponse response = new GetAccountTypeByIdResponse(accountType.id(), accountType.classificationId(), accountType.description(), accountType.active(), accountType.notes());
            ApiResponse<GetAccountTypeByIdResponse> apiResponse = new ApiResponse<>(String.format("Returned Account Type of ID:[%s]", id),new MetaData(1L),response);
            log.debug("Ending Rest Controller /accounttypes endpoint /byid id:[{}]",response.id());
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("AccountTypeController.getAccountTypeById failed for id:[{}]", id, ex);
            throw ex;
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CreateAccountTypeResponse>> createAccountType(@RequestBody CreateAccountTypeRequest request){
        try {
            log.debug("Starting Rest Controller /accounttypes endpoint /add classificationId:[{}] description:[{}]",request.classificationId(),request.description());
            CreateAccountTypeCommand command = new CreateAccountTypeCommand(request.classificationId(), request.description(), request.notes());
            AccountType accountType = createAccountTypeUseCase.createAccountType(command);
            CreateAccountTypeResponse response = new CreateAccountTypeResponse(accountType.id(), accountType.classificationId(), accountType.description(), accountType.active(), accountType.notes());
            ApiResponse<CreateAccountTypeResponse> apiResponse = new ApiResponse<>(String.format("Created Account Type [%s]", request.description()),new MetaData(1L),response);
            log.debug("Ending Rest Controller /accounttypes endpoint /add createdId:[{}]",response.id());
            return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            log.error("AccountTypeController.createAccountType failed for request:[{}]", request, ex);
            throw ex;
        }
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<UpdateAccountTypeResponse>> updateAccountType(@RequestBody UpdateAccountTypeRequest request){
        try {
            log.debug("Starting Rest Controller /accounttypes endpoint /update id:[{}] descriptiont:[{}]",request.id(),request.description());
            UpdateAccountTypeCommand command = new UpdateAccountTypeCommand(request.id(), request.description(),request.notes(),request.active());
            AccountType accountType = updateAccountTypeUseCase.updateAccountType(command);
            UpdateAccountTypeResponse response = new UpdateAccountTypeResponse(accountType.id(), accountType.classificationId(), accountType.description(), accountType.active(), accountType.notes());
            ApiResponse<UpdateAccountTypeResponse> apiResponse = new ApiResponse<>(String.format("Updated Account Type of ID:[%s]", request.id()),new MetaData(1L),response);
            log.debug("Ending Rest Controller /accounttypes endpoint /update updatedId:[{}]",response.id());
            return new ResponseEntity<>(apiResponse,HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("AccountTypeController.updateAccountType failed for request:[{}]", request, ex);
            throw ex;
        }
    }
}
