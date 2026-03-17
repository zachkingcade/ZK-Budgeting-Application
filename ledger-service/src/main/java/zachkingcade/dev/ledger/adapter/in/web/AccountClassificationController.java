package zachkingcade.dev.ledger.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zachkingcade.dev.ledger.adapter.in.web.dto.accountclassifcation.AccountClassificationObject;
import zachkingcade.dev.ledger.adapter.in.web.dto.accountclassifcation.GetAllAccountClassificationResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.accountclassifcation.GetByIdAccountClassificationResponse;
import zachkingcade.dev.ledger.application.port.in.accountclassification.GetAllAccountClassifcationsUseCase;
import zachkingcade.dev.ledger.application.port.in.accountclassification.GetByIdAccountClassificaitonUseCase;
import zachkingcade.dev.ledger.domain.account.AccountClassification;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/accountclassifications")
public class AccountClassificationController {

    private final GetAllAccountClassifcationsUseCase getAllAccountClassifcationsUseCase;
    private final GetByIdAccountClassificaitonUseCase getByIdAccountClassificaitonUseCase;

    public AccountClassificationController(GetAllAccountClassifcationsUseCase getAllAccountClassifcationsUseCase, GetByIdAccountClassificaitonUseCase getByIdAccountClassificaitonUseCase) {
        this.getAllAccountClassifcationsUseCase = getAllAccountClassifcationsUseCase;
        this.getByIdAccountClassificaitonUseCase = getByIdAccountClassificaitonUseCase;
    }

    @GetMapping("/all")
    public ResponseEntity<GetAllAccountClassificationResponse> getAll(){
        List<AccountClassification> domainList = getAllAccountClassifcationsUseCase.getAllAccountClassifications();
        List<AccountClassificationObject> resultingList = new ArrayList<>();
        for(AccountClassification accountClass: domainList){
            resultingList.add(new AccountClassificationObject(accountClass.id(), accountClass.description(),accountClass.creditEffect(),accountClass.debitEffect()));
        }
        GetAllAccountClassificationResponse response = new GetAllAccountClassificationResponse(resultingList);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/byid/{id}")
    public ResponseEntity<GetByIdAccountClassificationResponse> getById(@PathVariable Long id){
        AccountClassification accountClass = getByIdAccountClassificaitonUseCase.getByIdAccountClassifcation(id);
        GetByIdAccountClassificationResponse response = new GetByIdAccountClassificationResponse(accountClass.id(), accountClass.description(),accountClass.creditEffect(),accountClass.debitEffect());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
