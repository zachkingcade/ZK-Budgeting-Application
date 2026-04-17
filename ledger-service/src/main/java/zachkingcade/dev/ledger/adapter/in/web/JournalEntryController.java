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
import zachkingcade.dev.ledger.adapter.in.web.dto.journal.*;
import zachkingcade.dev.ledger.application.commands.account.GetAllAccountCommand;
import zachkingcade.dev.ledger.application.commands.account.GetByIdAccountCommand;
import zachkingcade.dev.ledger.application.commands.accounttype.AccountTypeFilterCommandObject;
import zachkingcade.dev.ledger.application.commands.accounttype.GetAllAccountTypesCommand;
import zachkingcade.dev.ledger.application.commands.accounttype.GetByIdAccountTypeCommand;
import zachkingcade.dev.ledger.application.commands.journal.*;
import zachkingcade.dev.ledger.application.commands.shared.SortObjectCommandObject;
import zachkingcade.dev.ledger.application.port.in.account.GetAllAccountsUseCase;
import zachkingcade.dev.ledger.application.port.in.account.GetByIdAccountUseCase;
import zachkingcade.dev.ledger.application.port.in.accountclassification.GetAllAccountClassificationsUseCase;
import zachkingcade.dev.ledger.application.port.in.accountclassification.GetByIdAccountClassificationUseCase;
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

    private final GetAllJournalEntryUseCase getAllJournalEntryUseCase;
    private final GetByIdJournalEntryUseCase getByIdJournalEntryUseCase;
    private final CreateJournalEntryUseCase createJournalEntryUseCase;
    private final UpdateJournalEntryUseCase updateJournalEntryUseCase;
    private final RemoveByIdJournalEntryUseCase removeByIdJournalEntryUseCase;
    private final GetAllAccountClassificationsUseCase getAllAccountClassificationsUseCase;
    private final GetAllAccountTypeUseCase getAllAccountTypeUseCase;
    private final GetAllAccountsUseCase getAllAccountsUseCase;
    private final GetByIdAccountClassificationUseCase getByIdAccountClassificationUseCase;
    private final GetByIdAccountUseCase getByIdAccountUseCase;
    private final GetByIdAccountTypeUseCase getByIdAccountTypeUseCase;
    private static final Logger log = LoggerFactory.getLogger(JournalEntryController.class);

    public JournalEntryController
            (GetAllJournalEntryUseCase getAllJournalEntryUseCase,
             GetByIdJournalEntryUseCase getByIdJournalEntryUseCase,
             CreateJournalEntryUseCase createJournalEntryUseCase,
             UpdateJournalEntryUseCase updateJournalEntryUseCase,
             RemoveByIdJournalEntryUseCase removeByIdJournalEntryUseCase,
             GetAllAccountClassificationsUseCase getAllAccountClassificationsUseCase,
             GetAllAccountTypeUseCase getAllAccountTypeUseCase,
             GetAllAccountsUseCase getAllAccountsUseCase,
             GetByIdAccountClassificationUseCase getByIdAccountClassificationUseCase,
             GetByIdAccountUseCase getByIdAccountUseCase,
             GetByIdAccountTypeUseCase getByIdAccountTypeUseCase)
    {
        this.getAllJournalEntryUseCase = getAllJournalEntryUseCase;
        this.getByIdJournalEntryUseCase = getByIdJournalEntryUseCase;
        this.createJournalEntryUseCase = createJournalEntryUseCase;
        this.updateJournalEntryUseCase = updateJournalEntryUseCase;
        this.removeByIdJournalEntryUseCase = removeByIdJournalEntryUseCase;
        this.getAllAccountClassificationsUseCase = getAllAccountClassificationsUseCase;
        this.getAllAccountTypeUseCase = getAllAccountTypeUseCase;
        this.getAllAccountsUseCase = getAllAccountsUseCase;
        this.getByIdAccountClassificationUseCase = getByIdAccountClassificationUseCase;
        this.getByIdAccountUseCase = getByIdAccountUseCase;
        this.getByIdAccountTypeUseCase = getByIdAccountTypeUseCase;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // Endpoints
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<GetAllJournalEntryResponse>> getAll(@AuthenticationPrincipal Jwt jwt){
        try {
            log.debug("Starting Rest Controller /journalentry endpoint /all");
            return handleGetAll(jwt, null);
        } catch (RuntimeException ex) {
            log.error("JournalEntryController.getAll failed", ex);
            throw ex;
        }
    }

    @PostMapping("/all/filtered")
    public ResponseEntity<ApiResponse<GetAllJournalEntryResponse>> getAllFiltered(@AuthenticationPrincipal Jwt jwt, @RequestBody(required = false) GetAllJournalEntryRequest request){
        try {
            log.debug("Starting Rest Controller /journalentry endpoint /all/filtered sortPresent:[{}] filtersPresent:[{}]",
                    request != null && request.sort().isPresent(),
                    request != null && request.filters().isPresent()
            );
            ResponseEntity<ApiResponse<GetAllJournalEntryResponse>> result = handleGetAll(jwt, request);
            Long count = null;
            if (result.getBody() != null && result.getBody().getMetaData() != null) {
                count = result.getBody().getMetaData().getDataResponseCount();
            }
            log.debug("Ending Rest Controller /journalentry endpoint /all/filtered count:[{}]", count);
            return result;
        } catch (RuntimeException ex) {
            log.error("JournalEntryController.getAllFiltered failed", ex);
            throw ex;
        }
    }

    private ResponseEntity<ApiResponse<GetAllJournalEntryResponse>> handleGetAll(Jwt jwt, GetAllJournalEntryRequest request){
        Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
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
                    request.filters().get().accounts(),
                    request.filters().get().searchContains()
            );
        } else {
            //default
            filters = new JournalEntryFilterCommandObject(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

        GetAllJournalEntriesCommand command = new GetAllJournalEntriesCommand(userId, Optional.of(sort), Optional.of(filters));
        List<JournalEntry> entryList = getAllJournalEntryUseCase.getAllJournalEntries(command);
        List<JournalEntryDTOEnrichedResponse> resultingEntryList = convertDomainListToResponseAndEnrich(userId, entryList);
        GetAllJournalEntryResponse response = new GetAllJournalEntryResponse(resultingEntryList);
        ApiResponse<GetAllJournalEntryResponse> apiResponse = new ApiResponse<>(String.format("Returned [%s] Journal Entries", resultingEntryList.size()),new MetaData((long) resultingEntryList.size()),response);
        log.debug("Ending Rest Controller /journalentry endpoint /all with [{}] results",resultingEntryList.size());
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/byid/{id}")
    public ResponseEntity<ApiResponse<GetByIdJournalEntryResponse>> getById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id){
        try {
            log.debug("Starting Rest Controller /journalentry endpoint /byid id:[{}]",id);
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            GetByIdJournalEntryCommand command = new GetByIdJournalEntryCommand(userId, id);
            JournalEntry entry = getByIdJournalEntryUseCase.getByIdJournalEntry(command);
            JournalEntryDTOEnrichedResponse enrichedEntry = convertDomainObjectToResponseAndEnrich(userId, entry);
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
    public ResponseEntity<ApiResponse<CreateJournalEntryResponse>> createJournalEntry(@AuthenticationPrincipal Jwt jwt, @RequestBody CreateJournalEntryRequest request){
        try {
            log.debug("Starting Rest Controller /journalentry endpoint /add description:[{}] journalLinesCount:[{}]",request.description(),request.journalLines() == null ? 0 : request.journalLines().size());
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            List<JournalLineCommandObject> resultingCommandLineList = new ArrayList<>();
            for(JournalLineDTORequest requestLine: request.journalLines()){
                resultingCommandLineList.add(new JournalLineCommandObject(requestLine.amount(), requestLine.accountId(), requestLine.direction(), requestLine.notes()));
            }
            CreateJournalEntryCommand command = new CreateJournalEntryCommand(userId, request.entryDate(),request.description(),request.notes(),resultingCommandLineList);
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
    public ResponseEntity<ApiResponse<UpdateJournalEntryResponse>> updateJournalEntry(@AuthenticationPrincipal Jwt jwt, @RequestBody UpdateJournalEntryRequest request){
        try {
            log.debug("Starting Rest Controller /journalentry endpoint /update id:[{}] description:[{}] requestedLineUpdatesCount:[{}]",request.id(),request.description(),request.journalLines() == null ? 0 : request.journalLines().size());
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            List<JournalLineUpdateCommandObject> resultingCommandLineList = new ArrayList<>();
            for(JournalLineDTOUpdate requestLine: request.journalLines()){
                resultingCommandLineList.add(new JournalLineUpdateCommandObject(requestLine.id(), requestLine.notes()));
            }
            UpdateJournalEntryCommand command = new UpdateJournalEntryCommand(
                    userId,
                    request.id(),
                    request.description(),
                    request.notes(),
                    resultingCommandLineList
            );
            JournalEntry entry = updateJournalEntryUseCase.updateJournalEntry(command);
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
    public ResponseEntity<ApiResponse<RemoveJournalEntryDTOResponse>> removeJournalEntry(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id){
        try{
            log.debug("Starting Rest Controller /journalentry endpoint /remove/{id} id:[{}]",id);
            Long userId = JwtPrincipalUserIdExtractor.extractEffectiveUserId(jwt);
            RemoveByIdJournalEntryCommand command = new RemoveByIdJournalEntryCommand(userId, id);
            removeByIdJournalEntryUseCase.removeJournalEntryById(command);
            RemoveJournalEntryDTOResponse response = new RemoveJournalEntryDTOResponse(id);
            ApiResponse<RemoveJournalEntryDTOResponse> apiResponse = new ApiResponse<>(String.format("Removed Journal Entry of ID:[%s]", id),new MetaData(0L),response);
            log.debug("Ending Rest Controller /journalentry endpoint /remove/{id} id:[{}]",id);
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("JournalEntryController.removeJournalEntry failed for id:[{}]", id, ex);
            throw ex;
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // Utility Functions
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private List<JournalEntryDTOEnrichedResponse> convertDomainListToResponseAndEnrich(Long userId, List<JournalEntry> journalEntryList ){
        //Collect needed data
        GetAllAccountCommand accountCommand = new GetAllAccountCommand(userId, Optional.empty(), Optional.empty());
        List<Account> accountList = getAllAccountsUseCase.getAllAccounts(accountCommand);
        Map<Long,Account> accountMap = accountList.stream().collect(Collectors.toMap(Account::id, account -> account));

        GetAllAccountTypesCommand typesCommand = new GetAllAccountTypesCommand(userId, Optional.empty(), Optional.empty());
        List<AccountType> typeList = getAllAccountTypeUseCase.getAllAccountTypes(typesCommand);
        Map<Long,AccountType> typeMap = typeList.stream().collect(Collectors.toMap(AccountType::id, accountType -> accountType));

        List<AccountClassification> classList = getAllAccountClassificationsUseCase.getAllAccountClassifications();
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

    private JournalEntryDTOEnrichedResponse convertDomainObjectToResponseAndEnrich(Long userId, JournalEntry entry){

        List<JournalLineDTOEnrichedResponse> lineList = new ArrayList<>();
        for(JournalLine line : entry.journalLines()){
            Account account = getByIdAccountUseCase.getAccountById(new GetByIdAccountCommand(userId, line.accountId()));
            AccountType type = getByIdAccountTypeUseCase.getAccountTypeById(new GetByIdAccountTypeCommand(userId, account.typeId()));
            AccountClassification classification = getByIdAccountClassificationUseCase.getByIdAccountClassification(type.classificationId());

            String accountName = account.description();
            String accountTypeName = type.description();
            String accountDisplayName = String.format("%s [%s]",accountName,accountTypeName);
            char lineAffectOnAccount = line.direction() == 'C'? classification.creditEffect() : classification.debitEffect();
            lineList.add(new JournalLineDTOEnrichedResponse(line.id(), line.amount(), line.accountId(),accountName, accountTypeName, accountDisplayName, lineAffectOnAccount, line.direction(), line.notes()));
        }

        return new JournalEntryDTOEnrichedResponse(entry.id(),entry.entryDate(),entry.description(),entry.notes(), lineList);
    }

}
