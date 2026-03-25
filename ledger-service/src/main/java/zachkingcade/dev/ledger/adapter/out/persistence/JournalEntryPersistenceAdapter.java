package zachkingcade.dev.ledger.adapter.out.persistence;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.JournalEntryEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.JournalLineEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.AccountJpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.JournalEntryJpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.JournalLinesJpaRepository;
import zachkingcade.dev.ledger.application.port.out.journal.JournalEntryRepositoryPort;
import zachkingcade.dev.ledger.domain.journal.JournalEntry;
import zachkingcade.dev.ledger.domain.journal.JournalLine;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class JournalEntryPersistenceAdapter implements JournalEntryRepositoryPort {

    JournalEntryJpaRepository journalEntryJpaRepository;
    JournalLinesJpaRepository journalLinesJpaRepository;
    AccountJpaRepository accountJpaRepository;

    public JournalEntryPersistenceAdapter(JournalEntryJpaRepository journalEntryJpaRepository, AccountJpaRepository accountJpaRepository, JournalLinesJpaRepository journalLinesJpaRepository) {
        this.journalEntryJpaRepository = journalEntryJpaRepository;
        this.accountJpaRepository = accountJpaRepository;
        this.journalLinesJpaRepository = journalLinesJpaRepository;
    }

    @Override
    public JournalEntry findById(Long id) {
        Optional<JournalEntryEntity> entity = journalEntryJpaRepository.findWithJournalLinesById(id);
        if(entity.isPresent()){
            return mapToDomain(entity.get());
        } else {
            throw new RuntimeException(String.format("Error: Journal Entry not found for Journal Entry id [%s]", id));
        }
    }

    @Override
    public List<JournalEntry> findAll() {
        List<JournalEntryEntity> list = journalEntryJpaRepository.findAll();
        return convertListOfJournalEntrysToDomain(list);
    }

    @Override
    public List<JournalEntry> findAll(Sort sort) {
        List<JournalEntryEntity> list = journalEntryJpaRepository.findAll(sort);
        return convertListOfJournalEntrysToDomain(list);
    }

    @Override
    public List<JournalEntry> findAll(Specification<JournalEntryEntity> spec) {
        List<JournalEntryEntity> list = journalEntryJpaRepository.findAll(spec);
        return convertListOfJournalEntrysToDomain(list);
    }

    @Override
    public List<JournalEntry> findAll(Specification<JournalEntryEntity> spec, Sort sort) {
        List<JournalEntryEntity> list = journalEntryJpaRepository.findAll(spec,sort);
        return convertListOfJournalEntrysToDomain(list);
    }

    private List<JournalEntry> convertListOfJournalEntrysToDomain(List<JournalEntryEntity> journalEntryEntityList){
        List<JournalEntry> resultingEntriesList = new ArrayList<>();
        for(JournalEntryEntity entry: journalEntryEntityList){
            resultingEntriesList.add(mapToDomain(entry));
        }
        return  resultingEntriesList;
    }

    @Override
    public void removeJournalEntry(Long id) {
        journalEntryJpaRepository.deleteById(id);
    }

    @Override
    public JournalEntry save(JournalEntry journalEntryToSave) {
         // Save Journal entry
        JournalEntryEntity entity = new JournalEntryEntity();
        if(journalEntryToSave.id() != null){
            entity.setId(journalEntryToSave.id());
        }
        entity.setEntryDate(Date.valueOf(journalEntryToSave.entryDate()));
        entity.setDescription(journalEntryToSave.description());
        entity.setNotes(journalEntryToSave.notes());

        // Save journal lines
        List<JournalLineEntity> lineEntityList = new ArrayList<>();
        for(JournalLine line: journalEntryToSave.journalLines()){
            AccountEntity account = this.accountJpaRepository.findById(line.accountId()).orElseThrow(() -> new RuntimeException(String.format("Error: Record not found for Account id [%s]", line.accountId())));
            JournalLineEntity newLine = new JournalLineEntity();
            if(line.id() != null){
                newLine.setId(line.id());
            }
            newLine.setAmount(line.amount());
            newLine.setDirection(line.direction());
            newLine.setAccount(account);
            newLine.setJournalEntry(entity);
            newLine.setNotes(line.notes());
            lineEntityList.add(newLine);
        }
        entity.setJournalLines(lineEntityList);
        JournalEntryEntity savedEntity = journalEntryJpaRepository.save(entity);

        return mapToDomain(savedEntity);
    }

    @Override
    public List<JournalLine> findLinesByAccountId(Long accountId) {
        return mapToDomain(journalLinesJpaRepository.findByAccountId(accountId));
    }

    private JournalEntry mapToDomain(JournalEntryEntity entity){
        return JournalEntry.rehydrate(entity.getId(), entity.getEntryDate().toLocalDate(),entity.getDescription(), entity.getNotes(), mapToDomain(entity.getJournalLines()));
    }

    private List<JournalLine> mapToDomain(List<JournalLineEntity> lineList){
        List<JournalLine> resultingLinesList = new ArrayList<>();
        for(JournalLineEntity line : lineList){
            resultingLinesList.add(JournalLine.rehydrate(line.getId(), line.getAmount(), line.getAccount().getId(),line.getDirection(), line.getNotes()));
        }
        return resultingLinesList;
    }
}
