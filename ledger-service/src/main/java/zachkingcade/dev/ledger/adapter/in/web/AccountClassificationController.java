package zachkingcade.dev.ledger.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zachkingcade.dev.ledger.adapter.in.web.dto.ApiResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.MetaData;
import zachkingcade.dev.ledger.adapter.in.web.dto.accountclassification.AccountClassificationObject;
import zachkingcade.dev.ledger.adapter.in.web.dto.accountclassification.GetAllAccountClassificationResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.accountclassification.GetByIdAccountClassificationResponse;
import zachkingcade.dev.ledger.application.port.in.accountclassification.GetAllAccountClassificationsUseCase;
import zachkingcade.dev.ledger.application.port.in.accountclassification.GetByIdAccountClassificationUseCase;
import zachkingcade.dev.ledger.domain.account.AccountClassification;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/accountclassifications")
public class AccountClassificationController {

    private static final Logger log = LoggerFactory.getLogger(AccountClassificationController.class);

    private final GetAllAccountClassificationsUseCase getAllAccountClassificationsUseCase;
    private final GetByIdAccountClassificationUseCase getByIdAccountClassificationUseCase;

    public AccountClassificationController(GetAllAccountClassificationsUseCase getAllAccountClassificationsUseCase, GetByIdAccountClassificationUseCase getByIdAccountClassificationUseCase) {
        this.getAllAccountClassificationsUseCase = getAllAccountClassificationsUseCase;
        this.getByIdAccountClassificationUseCase = getByIdAccountClassificationUseCase;
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<GetAllAccountClassificationResponse>> getAll(){
        try {
            log.debug("Starting Rest Controller /accountclassifications endpoint /all");
            List<AccountClassification> domainList = getAllAccountClassificationsUseCase.getAllAccountClassifications();
            List<AccountClassificationObject> resultingList = new ArrayList<>();
            for(AccountClassification accountClass: domainList){
                resultingList.add(new AccountClassificationObject(accountClass.id(), accountClass.description(),accountClass.creditEffect(),accountClass.debitEffect()));
            }
            GetAllAccountClassificationResponse response = new GetAllAccountClassificationResponse(resultingList);
            ApiResponse<GetAllAccountClassificationResponse> apiResponse = new ApiResponse<>(String.format("Returned [%s] Account Classifications", resultingList.size()),new MetaData((long) resultingList.size()),response);
            log.debug("Ending Rest Controller /accountclassifications endpoint /all with [{}] results",resultingList.size());
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("AccountClassificationController.getAll failed", ex);
            throw ex;
        }
    }

    @GetMapping("/byid/{id}")
    public ResponseEntity<ApiResponse<GetByIdAccountClassificationResponse>> getById(@PathVariable Long id){
        try {
            log.debug("Starting Rest Controller /accountclassifications endpoint /byid id:[{}]",id);
            AccountClassification accountClass = getByIdAccountClassificationUseCase.getByIdAccountClassification(id);
            GetByIdAccountClassificationResponse response = new GetByIdAccountClassificationResponse(accountClass.id(), accountClass.description(),accountClass.creditEffect(),accountClass.debitEffect());
            ApiResponse<GetByIdAccountClassificationResponse> apiResponse = new ApiResponse<>(String.format("Returned Account Classifications of ID:[%s]", id),new MetaData(1L),response);
            log.debug("Ending Rest Controller /accountclassifications endpoint /byid id:[{}]",response.id());
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("AccountClassificationController.getById failed for id:[{}]", id, ex);
            throw ex;
        }
    }
}
