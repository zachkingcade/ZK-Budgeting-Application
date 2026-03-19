package zachkingcade.dev.ledger.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zachkingcade.dev.ledger.adapter.in.web.dto.journal.*;
import zachkingcade.dev.ledger.application.AccountService;
import zachkingcade.dev.ledger.application.commands.journal.*;
import zachkingcade.dev.ledger.application.port.in.journal.CreateJournalEntryUseCase;
import zachkingcade.dev.ledger.application.port.in.journal.GetAllJournalEntryUsecase;
import zachkingcade.dev.ledger.application.port.in.journal.GetByIdJournalEntryUseCase;
import zachkingcade.dev.ledger.application.port.in.journal.UpdateJournalEntryUsecase;
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
    private static final Logger log = LoggerFactory.getLogger(JournalEntryController.class);

    public JournalEntryController(GetAllJournalEntryUsecase getAllJournalEntryUsecase, GetByIdJournalEntryUseCase getByIdJournalEntryUseCase, CreateJournalEntryUseCase createJournalEntryUseCase, UpdateJournalEntryUsecase updateJournalEntryUsecase) {
        this.getAllJournalEntryUsecase = getAllJournalEntryUsecase;
        this.getByIdJournalEntryUseCase = getByIdJournalEntryUseCase;
        this.createJournalEntryUseCase = createJournalEntryUseCase;
        this.updateJournalEntryUsecase = updateJournalEntryUsecase;
    }

    @GetMapping("/all")
    public ResponseEntity<GetAllJournalEntryResponse> getAll(){
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
            log.debug("Ending Rest Controller /journalentry endpoint /all with [{}] results",resultingEntryList.size());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("JournalEntryController.getAll failed", ex);
            throw ex;
        }
    }

    @GetMapping("/byid/{id}")
    public ResponseEntity<GetByIdJournalEntryResponse> getById(@PathVariable Long id){
        try {
            log.debug("Starting Rest Controller /journalentry endpoint /byid id:[{}]",id);
            GetByIdJournalEntryCommand command = new GetByIdJournalEntryCommand(id);
            JournalEntry entry = getByIdJournalEntryUseCase.getByIdJournalEntry(command);
            List<JournalLineDTOResponse> currentEntryLineList = new ArrayList<>();
            for(JournalLine line: entry.journalLines()){
                currentEntryLineList.add(new JournalLineDTOResponse(line.id(), line.amount(), line.accountId(), line.direction(), line.notes()));
            }
            GetByIdJournalEntryResponse response = new GetByIdJournalEntryResponse(entry.id(),entry.entryDate(),entry.description(),entry.notes(), currentEntryLineList);
            log.debug("Ending Rest Controller /journalentry endpoint /byid id:[{}] lineCount:[{}]",response.id(),currentEntryLineList.size());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("JournalEntryController.getById failed for id:[{}]", id, ex);
            throw ex;
        }
    }

    @PostMapping("/add")
    public ResponseEntity<CreateJournalEntryResponse> createJournalEntry(@RequestBody CreateJournalEntryRequest request){
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
            log.debug("Ending Rest Controller /journalentry endpoint /add createdId:[{}] journalLinesCount:[{}]",response.id(),currentEntryLineList.size());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            log.error("JournalEntryController.createJournalEntry failed for request:[{}]", request, ex);
            throw ex;
        }
    }

    @PostMapping("/update")
    public ResponseEntity<UpdateJournalEntryResponse> updateJournalEntry(@RequestBody UpdateJournalEntryRequest request){
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
            log.debug("Ending Rest Controller /journalentry endpoint /update updatedId:[{}] journalLinesCount:[{}]",response.id(),currentEntryLineList.size());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("JournalEntryController.updateJournalEntry failed for request:[{}]", request, ex);
            throw ex;
        }
    }
}
