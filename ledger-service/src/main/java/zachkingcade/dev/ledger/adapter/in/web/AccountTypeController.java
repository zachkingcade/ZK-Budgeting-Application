package zachkingcade.dev.ledger.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<GetAllAccountTypesResponse> getAllAccountTypes(){
        log.debug("Starting Rest Controller /accounttypes endpoint /all");
        List<AccountType> list = getallAccountTypeUseCase.getAllAccountTypes();

        List<AccountTypeObject> resultingList = new ArrayList<>();
        for(AccountType accountType: list){
            resultingList.add(new AccountTypeObject(accountType.id(),accountType.classificationId(),accountType.description(),accountType.active(),accountType.notes()));
        }

        GetAllAccountTypesResponse response = new GetAllAccountTypesResponse(resultingList);
        log.debug("Ending Rest Controller /accounttypes endpoint /all with [{}] results",resultingList.size());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/byid/{id}")
    public ResponseEntity<GetAccountTypeByIdResponse> getAccountTypeById(@PathVariable Long id){
        log.debug("Starting Rest Controller /accounttypes endpoint /byid id:[{}]",id);
        AccountType accountType = getbyidAccountTypeUseCase.getAccountTypeById(id);
        GetAccountTypeByIdResponse response = new GetAccountTypeByIdResponse(accountType.id(), accountType.classificationId(), accountType.description(), accountType.active(), accountType.notes());
        log.debug("Ending Rest Controller /accounttypes endpoint /byid id:[{}]",response.id());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<CreateAccountTypeResponse> createAccountType(@RequestBody CreateAccountTypeRequest request){
        log.debug("Starting Rest Controller /accounttypes endpoint /add classificationId:[{}] description:[{}]",request.classificationId(),request.description());
        CreateAccountTypeCommand command = new CreateAccountTypeCommand(request.classificationId(), request.description(), request.notes());
        AccountType accountType = createAccountTypeUseCase.createAccountType(command);
        CreateAccountTypeResponse response = new CreateAccountTypeResponse(accountType.id(), accountType.classificationId(), accountType.description(), accountType.active(), accountType.notes());
        log.debug("Ending Rest Controller /accounttypes endpoint /add createdId:[{}]",response.id());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<UpdateAccountTypeResponse> updateAccountType(@RequestBody UpdateAccountTypeRequest request){
        log.debug("Starting Rest Controller /accounttypes endpoint /update id:[{}] descriptiont:[{}]",request.id(),request.description());
        UpdateAccountTypeCommand command = new UpdateAccountTypeCommand(request.id(), request.description(),request.notes(),request.active());
        AccountType accountType = updateAccountTypeUseCase.updateAccountType(command);
        UpdateAccountTypeResponse response = new UpdateAccountTypeResponse(accountType.id(), accountType.classificationId(), accountType.description(), accountType.active(), accountType.notes());
        log.debug("Ending Rest Controller /accounttypes endpoint /update updatedId:[{}]",response.id());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
}
