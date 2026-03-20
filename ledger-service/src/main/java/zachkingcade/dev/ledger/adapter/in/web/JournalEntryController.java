package zachkingcade.dev.ledger.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zachkingcade.dev.ledger.adapter.in.web.dto.ApiResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.MetaData;
import zachkingcade.dev.ledger.adapter.in.web.dto.accountclassifcation.GetAllAccountClassificationResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.journal.*;
import zachkingcade.dev.ledger.application.AccountService;
import zachkingcade.dev.ledger.application.commands.journal.*;
import zachkingcade.dev.ledger.application.port.in.journal.*;
import zachkingcade.dev.ledger.domain.journal.JournalEntry;
import zachkingcade.dev.ledger.domain.journal.JournalLine;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/journalentry")
public class JournalEntryController {

    GetAllJournalEntryUsecase getAllJournalEntryUsecase;
    GetByIdJournalEntryUseCase getByIdJournalEntryUseCase;
    CreateJournalEntryUseCase createJournalEntryUseCase;
    UpdateJournalEntryUsecase updateJournalEntryUsecase;
    RemoveByIdJournalEntryUseCase removeByIdJournalEntryUseCase;
    private static final Logger log = LoggerFactory.getLogger(JournalEntryController.class);

    public JournalEntryController(GetAllJournalEntryUsecase getAllJournalEntryUsecase, GetByIdJournalEntryUseCase getByIdJournalEntryUseCase, CreateJournalEntryUseCase createJournalEntryUseCase, UpdateJournalEntryUsecase updateJournalEntryUsecase,RemoveByIdJournalEntryUseCase removeByIdJournalEntryUseCase) {
        this.getAllJournalEntryUsecase = getAllJournalEntryUsecase;
        this.getByIdJournalEntryUseCase = getByIdJournalEntryUseCase;
        this.createJournalEntryUseCase = createJournalEntryUseCase;
        this.updateJournalEntryUsecase = updateJournalEntryUsecase;
        this.removeByIdJournalEntryUseCase = removeByIdJournalEntryUseCase;
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<GetAllJournalEntryResponse>> getAll(){
        try {
            log.debug("Starting Rest Controller /journalentry endpoint /all");
            List<JournalEntry> entryList = getAllJournalEntryUsecase.getAllJournalEntries();
            List<JournalEntryDTOResponse> resultingEntryList = new ArrayList<>();
            for(JournalEntry entry : entryList){
                List<JournalLineDTOResponse> currentEntryLineList = new ArrayList<>();
                for(JournalLine line: entry.journalLines()){
                    currentEntryLineList.add(new JournalLineDTOResponse(line.id(), line.amount(), line.accountId(), line.direction(), line.notes()));
                }
                resultingEntryList.add(new JournalEntryDTOResponse(entry.id(),entry.entryDate(),entry.description(),entry.notes(), currentEntryLineList));
            }
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
            List<JournalLineDTOResponse> currentEntryLineList = new ArrayList<>();
            for(JournalLine line: entry.journalLines()){
                currentEntryLineList.add(new JournalLineDTOResponse(line.id(), line.amount(), line.accountId(), line.direction(), line.notes()));
            }
            GetByIdJournalEntryResponse response = new GetByIdJournalEntryResponse(entry.id(),entry.entryDate(),entry.description(),entry.notes(), currentEntryLineList);
            ApiResponse<GetByIdJournalEntryResponse> apiResponse = new ApiResponse<>(String.format("Returned Journal Entry of ID:[%s]", id),new MetaData(1L),response);
            log.debug("Ending Rest Controller /journalentry endpoint /byid id:[{}] lineCount:[{}]",response.id(),currentEntryLineList.size());
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
}
