package zachkingcade.dev.ledger.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zachkingcade.dev.ledger.adapter.in.web.dto.account.*;
import zachkingcade.dev.ledger.application.commands.account.CreateAccountCommand;
import zachkingcade.dev.ledger.application.commands.account.UpdateAccountCommand;
import zachkingcade.dev.ledger.application.port.in.account.CreateAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.account.GetByIdAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.account.GetAllAccountsUseCase;
import zachkingcade.dev.ledger.application.port.in.account.UpdateAccountUseCase;
import zachkingcade.dev.ledger.domain.account.Account;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

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
    public ResponseEntity<GetAllAccountsResponse> getAll(){
        List<Account> domainList = getallAccountsUseCase.getAllAccounts();
        List<AccountObject> resultingList = new ArrayList<>();
        for(Account account: domainList){
            resultingList.add(new AccountObject(account.id(), account.typeId(), account.description(),account.active(),account.notes()));
        }
        GetAllAccountsResponse response = new GetAllAccountsResponse(resultingList);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping("/byid/{id}")
    public ResponseEntity<GetAccountByIdResponse> getById(@PathVariable Long id){
        Account foundAccount = getByIdAccountUseCase.getAccountById(id);
        GetAccountByIdResponse response = new GetAccountByIdResponse(foundAccount.id(), foundAccount.typeId(), foundAccount.description(), foundAccount.active(), foundAccount.notes());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<CreateAccountResponse> create(@RequestBody CreateAccountRequest request) {
        CreateAccountCommand command = new CreateAccountCommand(request.typeId(), request.description(), request.notes());
        Account result = createAccountUseCase.createAccount(command);
        CreateAccountResponse response = new CreateAccountResponse(result.id(), result.typeId(), result.description(), result.active(), result.notes());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/update")
    public ResponseEntity<UpdateAccountResponse> updateAccount(@RequestBody UpdateAccountRequest request){
        UpdateAccountCommand command = new UpdateAccountCommand(request.id(), request.description(), request.notes(), request.active());
        Account result = updateAccountUseCase.updateAccount(command);
        UpdateAccountResponse response = new UpdateAccountResponse(request.id(), result.typeId(), result.description(), result.active(), result.notes());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
