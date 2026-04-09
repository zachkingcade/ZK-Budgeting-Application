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
import zachkingcade.dev.ledger.application.exception.NotFoundException;
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
    public JournalEntry findById(Long userId, Long id) {
        Optional<JournalEntryEntity> entity = journalEntryJpaRepository.findWithJournalLinesByIdAndUserId(id, userId);
        if(entity.isPresent()){
            return mapToDomain(entity.get());
        } else {
            throw new NotFoundException(String.format("Journal Entry not found for id [%s]", id));
        }
    }

    @Override
    public List<JournalEntry> findAll(Long userId) {
        List<JournalEntryEntity> list = journalEntryJpaRepository.findAllByUserId(userId);
        return convertListOfJournalEntrysToDomain(list);
    }

    @Override
    public List<JournalEntry> findAll(Long userId, Sort sort) {
        List<JournalEntryEntity> list = journalEntryJpaRepository.findAll((root, query, cb) -> cb.equal(root.get("userId"), userId), sort);
        return convertListOfJournalEntrysToDomain(list);
    }

    @Override
    public List<JournalEntry> findAll(Long userId, Specification<JournalEntryEntity> spec) {
        Specification<JournalEntryEntity> scopedSpec = ((Specification<JournalEntryEntity>) (root, query, cb) -> cb.equal(root.get("userId"), userId)).and(spec);
        List<JournalEntryEntity> list = journalEntryJpaRepository.findAll(scopedSpec);
        return convertListOfJournalEntrysToDomain(list);
    }

    @Override
    public List<JournalEntry> findAll(Long userId, Specification<JournalEntryEntity> spec, Sort sort) {
        Specification<JournalEntryEntity> scopedSpec = ((Specification<JournalEntryEntity>) (root, query, cb) -> cb.equal(root.get("userId"), userId)).and(spec);
        List<JournalEntryEntity> list = journalEntryJpaRepository.findAll(scopedSpec,sort);
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
    public void removeJournalEntry(Long userId, Long id) {
        Optional<JournalEntryEntity> existing = journalEntryJpaRepository.findById(id);
        if(existing.isEmpty() || !userId.equals(existing.get().getUserId())){
            throw new NotFoundException(String.format("Journal Entry not found for id [%s]", id));
        }
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
        entity.setUserId(journalEntryToSave.getUserId());

        // Save journal lines
        List<JournalLineEntity> lineEntityList = new ArrayList<>();
        for(JournalLine line: journalEntryToSave.journalLines()){
            AccountEntity account = this.accountJpaRepository.findByIdAndUserId(line.accountId(), journalEntryToSave.getUserId())
                    .orElseThrow(() -> new NotFoundException(String.format("Account not found for id [%s]", line.accountId())));
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
    public List<JournalLine> findLinesByAccountId(Long userId, Long accountId) {
        return mapToDomain(journalLinesJpaRepository.findByAccountIdAndJournalEntryUserId(accountId, userId));
    }

    private JournalEntry mapToDomain(JournalEntryEntity entity){
        return JournalEntry.rehydrate(entity.getId(), entity.getEntryDate().toLocalDate(),entity.getDescription(), entity.getNotes(), entity.getUserId(), mapToDomain(entity.getJournalLines()));
    }

    private List<JournalLine> mapToDomain(List<JournalLineEntity> lineList){
        List<JournalLine> resultingLinesList = new ArrayList<>();
        for(JournalLineEntity line : lineList){
            resultingLinesList.add(JournalLine.rehydrate(line.getId(), line.getAmount(), line.getAccount().getId(),line.getDirection(), line.getNotes()));
        }
        return resultingLinesList;
    }
}
