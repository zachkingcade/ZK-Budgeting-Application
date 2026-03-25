package zachkingcade.dev.ledger.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zachkingcade.dev.ledger.adapter.in.web.dto.ApiResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.MetaData;
import zachkingcade.dev.ledger.adapter.in.web.dto.journal.*;
import zachkingcade.dev.ledger.application.commands.account.GetAllAccountCommand;
import zachkingcade.dev.ledger.application.commands.accounttype.AccountTypeFilterCommandObject;
import zachkingcade.dev.ledger.application.commands.accounttype.GetAllAccountTypesCommand;
import zachkingcade.dev.ledger.application.commands.journal.*;
import zachkingcade.dev.ledger.application.commands.shared.SortObjectCommandObject;
import zachkingcade.dev.ledger.application.port.in.account.GetAllAccountsUseCase;
import zachkingcade.dev.ledger.application.port.in.account.GetByIdAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.accountclassification.GetAllAccountClassifcationsUseCase;
import zachkingcade.dev.ledger.application.port.in.accountclassification.GetByIdAccountClassificaitonUseCase;
import zachkingcade.dev.ledger.application.port.in.accounttype.GetAllAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.in.accounttype.GetByIdAccountTypeUseCase;
import zachkingcade.dev.ledger.application.port.in.journal.*;
import zachkingcade.dev.ledger.application.validation.JournalEntrySortType;
import zachkingcade.dev.ledger.application.validation.SortDirection;
import zachkingcade.dev.ledger.domain.account.Account;
import zachkingcade.dev.ledger.domain.account.AccountClassification;
import zachkingcade.dev.ledger.domain.account.AccountType;
import zachkingcade.dev.ledger.domain.journal.JournalEntry;
import zachkingcade.dev.ledger.domain.journal.JournalLine;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/journalentry")
public class JournalEntryController {

    private final GetAllJournalEntryUsecase getAllJournalEntryUsecase;
    private final GetByIdJournalEntryUseCase getByIdJournalEntryUseCase;
    private final CreateJournalEntryUseCase createJournalEntryUseCase;
    private final UpdateJournalEntryUsecase updateJournalEntryUsecase;
    private final RemoveByIdJournalEntryUseCase removeByIdJournalEntryUseCase;
    private final GetAllAccountClassifcationsUseCase getAllAccountClassifcationsUseCase;
    private final GetAllAccountTypeUseCase getallAccountTypeUseCase;
    private final GetAllAccountsUseCase getallAccountsUseCase;
    private final GetByIdAccountClassificaitonUseCase getByIdAccountClassificaitonUseCase;
    private final GetByIdAccountUseCase getByIdAccountUseCase;
    private final GetByIdAccountTypeUseCase getbyidAccountTypeUseCase;
    private static final Logger log = LoggerFactory.getLogger(JournalEntryController.class);

    public JournalEntryController
            (GetAllJournalEntryUsecase getAllJournalEntryUsecase,
             GetByIdJournalEntryUseCase getByIdJournalEntryUseCase,
             CreateJournalEntryUseCase createJournalEntryUseCase,
             UpdateJournalEntryUsecase updateJournalEntryUsecase,
             RemoveByIdJournalEntryUseCase removeByIdJournalEntryUseCase,
             GetAllAccountClassifcationsUseCase getAllAccountClassifcationsUseCase,
             GetAllAccountTypeUseCase getallAccountTypeUseCase,
             GetAllAccountsUseCase getallAccountsUseCase,
             GetByIdAccountClassificaitonUseCase getByIdAccountClassificaitonUseCase,
             GetByIdAccountUseCase getByIdAccountUseCase,
             GetByIdAccountTypeUseCase getbyidAccountTypeUseCase)
    {
        this.getAllJournalEntryUsecase = getAllJournalEntryUsecase;
        this.getByIdJournalEntryUseCase = getByIdJournalEntryUseCase;
        this.createJournalEntryUseCase = createJournalEntryUseCase;
        this.updateJournalEntryUsecase = updateJournalEntryUsecase;
        this.removeByIdJournalEntryUseCase = removeByIdJournalEntryUseCase;
        this.getAllAccountClassifcationsUseCase = getAllAccountClassifcationsUseCase;
        this.getallAccountTypeUseCase = getallAccountTypeUseCase;
        this.getallAccountsUseCase = getallAccountsUseCase;
        this.getByIdAccountClassificaitonUseCase = getByIdAccountClassificaitonUseCase;
        this.getByIdAccountUseCase = getByIdAccountUseCase;
        this.getbyidAccountTypeUseCase = getbyidAccountTypeUseCase;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // Endpoints
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<GetAllJournalEntryResponse>> getAll(@RequestBody(required = false) GetAllJournalEntryRequest request){
        try {
            log.debug("Starting Rest Controller /journalentry endpoint /all");
            // Sanitize Request
            SortObjectCommandObject<JournalEntrySortType> sort = null;
            if(request != null && request.sort().isPresent()){
                sort = new SortObjectCommandObject<>(request.sort().get().type(), request.sort().get().direction() != null? request.sort().get().direction() : SortDirection.ascending );
            } else {
                //default
                sort = new SortObjectCommandObject<>(JournalEntrySortType.entryDate, SortDirection.ascending);
            }

            // Sanitize Request Filters
            JournalEntryFilterCommandObject filters = null;
            if(request != null && request.filters().isPresent()){
                filters = new JournalEntryFilterCommandObject(
                        request.filters().get().dateAfter(),
                        request.filters().get().dateBefore(),
                        request.filters().get().descriptionContains(),
                        request.filters().get().notesContains(),
                        request.filters().get().accountTypes(),
                        request.filters().get().accounts()
                );
            } else {
                //default
                filters = new JournalEntryFilterCommandObject(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty());
            }

            GetAllJournalEntrysCommand command = new GetAllJournalEntrysCommand(Optional.of(sort), Optional.of(filters));
            List<JournalEntry> entryList = getAllJournalEntryUsecase.getAllJournalEntries(command);
            List<JournalEntryDTOEnrichedResponse> resultingEntryList = convertDomainListToResponseAndEnrich(entryList);
            GetAllJournalEntryResponse response = new GetAllJournalEntryResponse(resultingEntryList);
            ApiResponse<GetAllJournalEntryResponse> apiResponse = new ApiResponse<>(String.format("Returned [%s] Journal Entries", resultingEntryList.size()),new MetaData((long) resultingEntryList.size()),response);
            log.debug("Ending Rest Controller /journalentry endpoint /all with [{}] results",resultingEntryList.size());
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("JournalEntryController.getAll failed", ex);
            throw ex;
        }
    }

    @GetMapping("/byid/{id}")
    public ResponseEntity<ApiResponse<GetByIdJournalEntryResponse>> getById(@PathVariable Long id){
        try {
            log.debug("Starting Rest Controller /journalentry endpoint /byid id:[{}]",id);
            GetByIdJournalEntryCommand command = new GetByIdJournalEntryCommand(id);
            JournalEntry entry = getByIdJournalEntryUseCase.getByIdJournalEntry(command);
            JournalEntryDTOEnrichedResponse enrichedEntry = convertDomainObjectToResponseAndEnrich(entry);
            GetByIdJournalEntryResponse response = new GetByIdJournalEntryResponse(enrichedEntry.id(),enrichedEntry.entryDate(),enrichedEntry.description(),enrichedEntry.notes(), enrichedEntry.journalLines());
            ApiResponse<GetByIdJournalEntryResponse> apiResponse = new ApiResponse<>(String.format("Returned Journal Entry of ID:[%s]", id),new MetaData(1L),response);
            log.debug("Ending Rest Controller /journalentry endpoint /byid id:[{}] lineCount:[{}]",response.id(),enrichedEntry.journalLines().size());
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("JournalEntryController.getById failed for id:[{}]", id, ex);
            throw ex;
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CreateJournalEntryResponse>> createJournalEntry(@RequestBody CreateJournalEntryRequest request){
        try {
            log.debug("Starting Rest Controller /journalentry endpoint /add description:[{}] journalLinesCount:[{}]",request.description(),request.journalLines() == null ? 0 : request.journalLines().size());
            List<JournalLineCommandObject> resultingCommandLineList = new ArrayList<>();
            for(JournalLineDTORequest requestLine: request.journalLines()){
                resultingCommandLineList.add(new JournalLineCommandObject(requestLine.amount(), requestLine.accountId(), requestLine.direction(), requestLine.notes()));
            }
            CreateJournalEntryCommand command = new CreateJournalEntryCommand(request.entryDate(),request.description(),request.notes(),resultingCommandLineList);
            JournalEntry entry = createJournalEntryUseCase.createJournalEntry(command);
            List<JournalLineDTOResponse> currentEntryLineList = new ArrayList<>();
            for(JournalLine line: entry.journalLines()){
                currentEntryLineList.add(new JournalLineDTOResponse(line.id(), line.amount(), line.accountId(), line.direction(), line.notes()));
            }
            CreateJournalEntryResponse response = new CreateJournalEntryResponse(entry.id(),entry.entryDate(),entry.description(),entry.notes(), currentEntryLineList);
            ApiResponse<CreateJournalEntryResponse> apiResponse = new ApiResponse<>(String.format("Created Journal Entry [%s]", request.description()),new MetaData(1L),response);
            log.debug("Ending Rest Controller /journalentry endpoint /add createdId:[{}] journalLinesCount:[{}]",response.id(),currentEntryLineList.size());
            return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            log.error("JournalEntryController.createJournalEntry failed for request:[{}]", request, ex);
            throw ex;
        }
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<UpdateJournalEntryResponse>> updateJournalEntry(@RequestBody UpdateJournalEntryRequest request){
        try {
            log.debug("Starting Rest Controller /journalentry endpoint /update id:[{}] description:[{}] requestedLineUpdatesCount:[{}]",request.id(),request.description(),request.journalLines() == null ? 0 : request.journalLines().size());
            List<JournalLineUpdateCommandObject> resultingCommandLineList = new ArrayList<>();
            for(JournalLineDTOUpdate requestLine: request.journalLines()){
                resultingCommandLineList.add(new JournalLineUpdateCommandObject(requestLine.id(), requestLine.notes()));
            }
            UpdateJournalEntryCommand command = new UpdateJournalEntryCommand(
                    request.id(),
                    request.description(),
                    request.notes(),
                    resultingCommandLineList
            );
            JournalEntry entry = updateJournalEntryUsecase.updateJournalEntry(command);
            List<JournalLineDTOResponse> currentEntryLineList = new ArrayList<>();
            for(JournalLine line: entry.journalLines()){
                currentEntryLineList.add(new JournalLineDTOResponse(line.id(), line.amount(), line.accountId(), line.direction(), line.notes()));
            }
            UpdateJournalEntryResponse response = new UpdateJournalEntryResponse(entry.id(),entry.entryDate(),entry.description(),entry.notes(), currentEntryLineList);
            ApiResponse<UpdateJournalEntryResponse> apiResponse = new ApiResponse<>(String.format("Updated Journal Entry of ID:[%s]", request.id()),new MetaData(1L),response);
            log.debug("Ending Rest Controller /journalentry endpoint /update updatedId:[{}] journalLinesCount:[{}]",response.id(),currentEntryLineList.size());
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("JournalEntryController.updateJournalEntry failed for request:[{}]", request, ex);
            throw ex;
        }
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<ApiResponse<RemoveJournalEntryDTOResponse>> removeJournalEntry(@PathVariable Long id){
        try{
            log.debug("Starting Rest Controller /journalentry endpoint /remove/{id} id:[{}]",id);
            RemoveByIdJournalEntryCommand command = new RemoveByIdJournalEntryCommand(id);
            removeByIdJournalEntryUseCase.removeJournalEntryById(command);
            RemoveJournalEntryDTOResponse response = new RemoveJournalEntryDTOResponse(id);
            ApiResponse<RemoveJournalEntryDTOResponse> apiResponse = new ApiResponse<>(String.format("Removed Journal Entry of ID:[%s]", id),new MetaData(0L),response);
            log.debug("Ending Rest Controller /journalentry endpoint /remove/{id} id:[{}]",id);
            return new ResponseEntity<>(apiResponse, HttpStatus.NO_CONTENT);
        } catch (RuntimeException ex) {
            log.error("JournalEntryController.removeJournalEntry failed for id:[{}]", id, ex);
            throw new RuntimeException(ex);
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // Utility Functions
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private List<JournalEntryDTOEnrichedResponse> convertDomainListToResponseAndEnrich(List<JournalEntry> journalEntryList ){
        //Collect needed data
        GetAllAccountCommand accountCommand = new GetAllAccountCommand(Optional.empty(), Optional.empty());
        List<Account> accountList = getallAccountsUseCase.getAllAccounts(accountCommand);
        Map<Long,Account> accountMap = accountList.stream().collect(Collectors.toMap(Account::id, account -> account));

        GetAllAccountTypesCommand typesCommand = new GetAllAccountTypesCommand(Optional.empty(), Optional.empty());
        List<AccountType> typeList = getallAccountTypeUseCase.getAllAccountTypes(typesCommand);
        Map<Long,AccountType> typeMap = typeList.stream().collect(Collectors.toMap(AccountType::id, accountType -> accountType));

        List<AccountClassification> classList = getAllAccountClassifcationsUseCase.getAllAccountClassifications();
        Map<Long,AccountClassification> classMap = classList.stream().collect(Collectors.toMap(AccountClassification::id, classification -> classification));


        //Convert and Enrich
        List<JournalEntryDTOEnrichedResponse> resultingEntryList = new ArrayList<>();
        for(JournalEntry entry : journalEntryList){
            List<JournalLineDTOEnrichedResponse> currentEntryLineList = new ArrayList<>();
            for(JournalLine line: entry.journalLines()){

                Account account = accountMap.get(line.accountId());
                AccountType type = typeMap.get(account.typeId());
                AccountClassification classification = classMap.get(type.classificationId());

                String accountName = account.description();
                String accountTypeName = type.description();
                String accountDisplayName = String.format("%s [%s]",accountName,accountTypeName);
                char lineAffectOnAccount = line.direction() == 'C'? classification.creditEffect() : classification.debitEffect();
                currentEntryLineList.add(new JournalLineDTOEnrichedResponse(line.id(), line.amount(), line.accountId(),accountName, accountTypeName, accountDisplayName, lineAffectOnAccount, line.direction(), line.notes()));
            }
            resultingEntryList.add(new JournalEntryDTOEnrichedResponse(entry.id(),entry.entryDate(),entry.description(),entry.notes(), currentEntryLineList));
        }
        return resultingEntryList;
    }

    private JournalEntryDTOEnrichedResponse convertDomainObjectToResponseAndEnrich(JournalEntry entry){

        List<JournalLineDTOEnrichedResponse> lineList = new ArrayList<>();
        for(JournalLine line : entry.journalLines()){
            Account account = getByIdAccountUseCase.getAccountById(line.accountId());
            AccountType type = getbyidAccountTypeUseCase.getAccountTypeById(account.typeId());
            AccountClassification classification = getByIdAccountClassificaitonUseCase.getByIdAccountClassifcation(type.classificationId());

            String accountName = account.description();
            String accountTypeName = type.description();
            String accountDisplayName = String.format("%s [%s]",accountName,accountTypeName);
            char lineAffectOnAccount = line.direction() == 'C'? classification.creditEffect() : classification.debitEffect();
            lineList.add(new JournalLineDTOEnrichedResponse(line.id(), line.amount(), line.accountId(),accountName, accountTypeName, accountDisplayName, lineAffectOnAccount, line.direction(), line.notes()));
        }

        return new JournalEntryDTOEnrichedResponse(entry.id(),entry.entryDate(),entry.description(),entry.notes(), lineList);
    }
}
