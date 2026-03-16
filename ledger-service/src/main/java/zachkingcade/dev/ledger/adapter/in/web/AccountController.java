package zachkingcade.dev.ledger.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zachkingcade.dev.ledger.adapter.in.web.dto.account.*;
import zachkingcade.dev.ledger.application.AccountService;
import zachkingcade.dev.ledger.application.commands.account.CreateAccountCommand;
import zachkingcade.dev.ledger.application.commands.account.GetByIdAccountCommand;
import zachkingcade.dev.ledger.application.commands.account.UpdateAccountCommand;
import zachkingcade.dev.ledger.domain.account.Account;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    
    private final AccountService accountService;


    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/all")
    public ResponseEntity<GetAllAccountsResponse> getAll(){
        List<Account> domainList = accountService.getAllAccounts();
        List<AccountObject> resultingList = new ArrayList<>();
        for(Account account: domainList){
            resultingList.add(new AccountObject(account.id(), account.typeId(), account.description(),account.active(),account.notes()));
        }
        GetAllAccountsResponse response = new GetAllAccountsResponse(resultingList);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping("/byid")
    public ResponseEntity<GetAccountByIdResponse> getById(@RequestBody GetAccountByIdRequest request){
        GetByIdAccountCommand command = new GetByIdAccountCommand(request.id());
        Account foundAccount = accountService.getAccountById(command);
        GetAccountByIdResponse response = new GetAccountByIdResponse(foundAccount.id(), foundAccount.typeId(), foundAccount.description(), foundAccount.active(), foundAccount.notes());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<CreateAccountResponse> create(@RequestBody CreateAccountRequest request){
        CreateAccountCommand command = new CreateAccountCommand(request.typeId(), request.description(), request.notes());
        Account result = accountService.createAccount(command);
        CreateAccountResponse response = new CreateAccountResponse(result.id(), result.typeId(), result.description(), result.active(), result.notes());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/update")
    public ResponseEntity<UpdateAccountResponse> updateAccount(@RequestBody UpdateAccountRequest request){
        UpdateAccountCommand command = new UpdateAccountCommand(request.id(), request.description(), request.notes(), request.active());
        Account result = accountService.updateAccount(command);
        UpdateAccountResponse response = new UpdateAccountResponse(request.id(), result.typeId(), result.description(), result.active(), result.notes());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
