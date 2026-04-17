package zachkingcade.dev.ledger.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.JournalEntryEntity;
import zachkingcade.dev.ledger.application.commands.journal.*;
import zachkingcade.dev.ledger.application.port.in.journal.*;
import zachkingcade.dev.ledger.application.port.out.journal.JournalEntryRepositoryPort;
import zachkingcade.dev.ledger.application.validation.SortDirection;
import zachkingcade.dev.ledger.domain.account.Account;
import zachkingcade.dev.ledger.domain.account.AccountClassification;
import zachkingcade.dev.ledger.domain.account.AccountType;
import zachkingcade.dev.ledger.domain.journal.JournalEntry;
import zachkingcade.dev.ledger.domain.journal.JournalLine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static zachkingcade.dev.ledger.adapter.out.persistence.specification.JournalEntrySpecifications.*;

@Service
public class JournalEntryService implements CreateJournalEntryUseCase, GetAllJournalEntryUseCase, GetByIdJournalEntryUseCase, UpdateJournalEntryUseCase, RemoveByIdJournalEntryUseCase, GetBalanceForAccountUseCase {

    private final JournalEntryRepositoryPort journalEntryRepository;
    private static final Logger log = LoggerFactory.getLogger(JournalEntryService.class);

    public JournalEntryService(JournalEntryRepositoryPort journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    public JournalEntry createJournalEntry(CreateJournalEntryCommand command) {
        try {
            log.debug("Starting Create Journal Entry entryDate:[{}] description:[{}] journalLinesCount:[{}]",command.entryDate(),command.description(),command.journalLinesList().size());
            List<JournalLine> lineList = new ArrayList<>();
            for(JournalLineCommandObject line : command.journalLinesList()){
                lineList.add(JournalLine.createNew(line.amount(), line.accountId(), line.direction(), line.notes().orElse("")));
            }
            JournalEntry entry = JournalEntry.createNew(command.entryDate(), command.description(), command.notes().orElse(""), command.userId(), lineList);
            JournalEntry saved = journalEntryRepository.save(entry);
            log.debug("Ending Create Journal Entry createdId:[{}] journalLinesCount:[{}]",saved.id(),saved.journalLines().size());
            return saved;
        } catch (RuntimeException ex) {
            log.error("JournalEntryService.createJournalEntry failed for command:[{}]", command, ex);
            throw ex;
        }
    }

    @Override
    public List<JournalEntry> getAllJournalEntries(GetAllJournalEntriesCommand command) {
        try {
            log.debug("Starting Get All Journal Entries");

            List<JournalEntry> results;
            Sort sort = null;
            Specification<JournalEntryEntity> spec = Specification.where(belongsToUser(command.userId()));

            if(command.sort().isPresent()){
                sort = Sort.by(command.sort().get().direction() == SortDirection.ascending? Sort.Direction.ASC : Sort.Direction.DESC, command.sort().get().type().toString());
            }

            if(command.filters().isPresent()){
                var f = command.filters().get();
                boolean useSearch = f.searchContains().isPresent()
                        && f.searchContains().get() != null
                        && !f.searchContains().get().isBlank();
                if (useSearch) {
                    spec = spec
                            .and(searchContainsEntryOrLineNotes(f.searchContains().get().trim()))
                            .and(dateAfter(f.dateAfter().orElse(null)))
                            .and(dateBefore(f.dateBefore().orElse(null)))
                            .and(accountIdsWithin(f.accounts().orElse(null)))
                            .and(accountTypeIdsWithin(f.accountTypes().orElse(null)));
                } else {
                    spec = spec
                            .and(descriptionContains(f.descriptionContains().orElse(null)))
                            .and(dateAfter(f.dateAfter().orElse(null)))
                            .and(dateBefore(f.dateBefore().orElse(null)))
                            .and(accountIdsWithin(f.accounts().orElse(null)))
                            .and(accountTypeIdsWithin(f.accountTypes().orElse(null)))
                            .and(notesContains(f.notesContains().orElse(null)));
                }
            }

            if(sort != null){
                results = journalEntryRepository.findAll(command.userId(), spec, sort);
            } else {
                results = journalEntryRepository.findAll(command.userId(), spec);
            }

            log.debug("Ending Get All Journal Entries results:[{}]",results.size());
            return results;
        } catch (RuntimeException ex) {
            log.error("JournalEntryService.getAllJournalEntries failed", ex);
            throw ex;
        }
    }

    @Override
    public JournalEntry getByIdJournalEntry(GetByIdJournalEntryCommand command) {
        try {
            log.debug("Starting Get Journal Entry by id:[{}]",command.id());
            JournalEntry result = journalEntryRepository.findById(command.userId(), command.id());
            log.debug("Ending Get Journal Entry by id:[{}] lineCount:[{}]",result.id(),result.journalLines().size());
            return result;
        } catch (RuntimeException ex) {
            log.error("JournalEntryService.getByIdJournalEntry failed for command:[{}]", command, ex);
            throw ex;
        }
    }

    @Override
    public JournalEntry updateJournalEntry(UpdateJournalEntryCommand command) {
        try {
            log.debug("Starting Update Journal Entry jeId:[{}] description:[{}]",command.id(),command.description());
            JournalEntry entryToUpdate = journalEntryRepository.findById(command.userId(), command.id());
            List<JournalLine> updatedLineList;

            if(!command.journalLinesList().isEmpty()) {
                log.debug("Preparing Journal Line corrections jeId:[{}] originalLinesCount:[{}] requestedLineUpdatesCount:[{}]",command.id(),entryToUpdate.journalLines().size(),command.journalLinesList().size());
                updatedLineList = new ArrayList<>();
                Set<Long> updatedLineIds = new HashSet<>();
                // Add lines requested to updated
                for (JournalLineUpdateCommandObject lineUpdate : command.journalLinesList()) {
                    for (JournalLine originalLine : entryToUpdate.journalLines()) {
                        if (lineUpdate.id().equals(originalLine.id())) {
                            updatedLineIds.add(originalLine.id());
                            updatedLineList.add(JournalLine.rehydrate(
                                    originalLine.id(),
                                    originalLine.amount(),
                                    originalLine.accountId(),
                                    originalLine.direction(),
                                    lineUpdate.notes()
                            ));
                            break;
                        }
                    }
                }
                // Add lines not changed
                for (JournalLine originalLine : entryToUpdate.journalLines()) {
                    if (!updatedLineIds.contains(originalLine.id())) {
                        updatedLineList.add(originalLine);
                    }
                }
                log.debug("Prepared Journal Line corrections jeId:[{}] originalLinesCount:[{}] resultingLinesCount:[{}]",command.id(),entryToUpdate.journalLines().size(),updatedLineList.size());
            } else {
                log.debug("No Journal Line updates for JE jeId:[{}] originalLinesCount:[{}]",command.id(),entryToUpdate.journalLines().size());
                updatedLineList = entryToUpdate.journalLines();
            }

            JournalEntry entry = JournalEntry.rehydrate(
                    command.id(),
                    entryToUpdate.entryDate(),
                    command.description().orElse(entryToUpdate.description()),
                    command.notes().orElse(entryToUpdate.notes()),
                    entryToUpdate.getUserId(),
                    updatedLineList
                    );

            JournalEntry saved = journalEntryRepository.save(entry);
            log.debug("Ending Update Journal Entry updatedId:[{}] journalLinesCount:[{}]",saved.id(),saved.journalLines().size());
            return saved;
        } catch (RuntimeException ex) {
            log.error("JournalEntryService.updateJournalEntry failed for command:[{}]", command, ex);
            throw ex;
        }
    }


    @Override
    public void removeJournalEntryById(RemoveByIdJournalEntryCommand command) {
        try {
            log.debug("Starting remove Journal Entry jeId:[{}]",command.id());
            journalEntryRepository.removeJournalEntry(command.userId(), command.id());
            log.debug("Ending remove Journal Entry jeId:[{}]",command.id());
        } catch (RuntimeException ex) {
            log.error("JournalEntryService.removeJournalEntryById failed for command:[{}]", command, ex);
            throw ex;
        }
    }

    @Override
    public Long getBalanceForAccount(Long userId, Long accountId, AccountClassification classification){
        List<JournalLine> lineList = journalEntryRepository.findLinesByAccountId(userId, accountId);
        Long credit = 0L;
        Long debit = 0L;
        for(JournalLine line : lineList){
            if(line.direction() == 'C'){
                credit += line.amount();
            } else {
                debit += line.amount();
            }
        }

        long resultingTotal = 0L;

        if(classification.creditEffect() == '+'){
            resultingTotal += credit;
        } else {
            resultingTotal -= credit;
        }

        if(classification.debitEffect() == '+'){
            resultingTotal += debit;
        } else {
            resultingTotal -= debit;
        }

        return resultingTotal;
    }
}
