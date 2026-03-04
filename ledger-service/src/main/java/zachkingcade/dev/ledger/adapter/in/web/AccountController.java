package zachkingcade.dev.ledger.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zachkingcade.dev.ledger.adapter.in.web.dto.CreateAccountRequest;
import zachkingcade.dev.ledger.adapter.in.web.dto.CreateAccountResponse;
import zachkingcade.dev.ledger.application.AccountService;
import zachkingcade.dev.ledger.application.commands.CreateAccountCommand;
import zachkingcade.dev.ledger.domain.account.Account;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    
    private final AccountService accountService;


    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/add")
    public ResponseEntity<CreateAccountResponse> create(@RequestBody CreateAccountRequest request){
        CreateAccountCommand command = new CreateAccountCommand(request.typeId(), request.description(), request.notes());
        Account result = accountService.createAccount(command);
        CreateAccountResponse response = new CreateAccountResponse(result.id(), result.typeId(), result.description(), result.active(), result.notes());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
