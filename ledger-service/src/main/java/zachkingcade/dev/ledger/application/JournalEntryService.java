package zachkingcade.dev.ledger.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.in.web.dto.GlobalExceptionHandler;
import zachkingcade.dev.ledger.application.commands.journal.*;
import zachkingcade.dev.ledger.application.port.in.journal.CreateJournalEntryUseCase;
import zachkingcade.dev.ledger.application.port.in.journal.GetAllJournalEntryUsecase;
import zachkingcade.dev.ledger.application.port.in.journal.GetByIdJournalEntryUseCase;
import zachkingcade.dev.ledger.application.port.in.journal.UpdateJournalEntryUsecase;
import zachkingcade.dev.ledger.application.port.out.journal.JournalEntryRepositoryPort;
import zachkingcade.dev.ledger.domain.journal.JournalEntry;
import zachkingcade.dev.ledger.domain.journal.JournalLine;

import java.util.ArrayList;
import java.util.List;

@Service
public class JournalEntryService implements CreateJournalEntryUseCase, GetAllJournalEntryUsecase, GetByIdJournalEntryUseCase, UpdateJournalEntryUsecase {

    private final JournalEntryRepositoryPort journalEntryRepository;
    private static final Logger log = LoggerFactory.getLogger(JournalEntryService.class);

    public JournalEntryService(JournalEntryRepositoryPort journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    public JournalEntry createJournalEntry(CreateJournalEntryCommand command) {
        log.info("Starting Create Journal Entry for new JE [{}]", command.description());
        List<JournalLine> lineList = new ArrayList<>();
        for(JournalLineCommandObject line : command.journalLinesList()){
            lineList.add(JournalLine.createNew(line.amount(), line.accountId(), line.direction(), line.notes()));
        }
        JournalEntry entry = JournalEntry.createNew(command.entryDate(), command.description(), command.notes(),lineList);
        return journalEntryRepository.save(entry);
    }

    @Override
    public List<JournalEntry> getAllJournalEntries() {
        return journalEntryRepository.findAll();
    }

    @Override
    public JournalEntry getByIdJournalEntry(GetByIdJournalEntryCommand command) {
        return journalEntryRepository.findById(command.id());
    }

    @Override
    public JournalEntry updateJournalEntry(UpdateJournalEntryCommand command) {
        log.info("Starting Update Journal Entry for JE [{}][{}]",command.id(), command.description());
        JournalEntry entryToUpdate = journalEntryRepository.findById(command.id());
        List<JournalLine> updatedLineList;

        if(!command.journalLinesList().isEmpty()) {
            log.info("Preparing Journal Line correcitons for JE [{}][{}]",command.id(), command.description());
            updatedLineList = new ArrayList<>();
            // Add lines requested to updated
            for (JournalLineUpdateCommandObject lineUpdate : command.journalLinesList()) {
                for (JournalLine originalLine : entryToUpdate.journalLines()) {
                    if (lineUpdate.id().equals(originalLine.id())) {
                        updatedLineList.add(JournalLine.rehydrate(
                                originalLine.id(),
                                originalLine.amount(),
                                originalLine.accountId(),
                                originalLine.direction(),
                                lineUpdate.notes()
                        ));
                    }
                }
            }
            // Add lines not changed
            for (JournalLine originalLine : entryToUpdate.journalLines()) {
                boolean foundLine = false;
                for (JournalLine newLine : updatedLineList) {
                    if (newLine.id().equals(originalLine.id())) {
                        foundLine = true;
                    }
                }
                if (!foundLine) {
                    updatedLineList.add(originalLine);
                }
            }
        } else {
            log.info("No Journal Line updates for JE [{}][{}]",command.id(), command.description());
            updatedLineList = entryToUpdate.journalLines();
        }

        JournalEntry entry = JournalEntry.rehydrate(
                command.id(),
                entryToUpdate.entryDate(),
                command.description().orElse(entryToUpdate.description()),
                command.notes().orElse(entryToUpdate.notes()),
                updatedLineList
                );

        return journalEntryRepository.save(entry);
    }

}
