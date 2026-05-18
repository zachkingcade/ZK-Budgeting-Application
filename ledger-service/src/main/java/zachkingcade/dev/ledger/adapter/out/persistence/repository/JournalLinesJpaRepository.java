package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.JournalEntryEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.JournalLineEntity;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

public interface JournalLinesJpaRepository extends JpaRepository<JournalLineEntity, Long> {
    List<JournalLineEntity> findByJournalEntry(JournalEntryEntity entity);

    List<JournalLineEntity> findByAccountId(Long accountId);

    List<JournalLineEntity> findByAccountIdAndJournalEntryUserId(Long accountId, Long userId);

    @Query("""
            SELECT jl FROM JournalLineEntity jl
            JOIN jl.journalEntry je
            WHERE jl.account.id = :accountId
            AND je.userId = :userId
            AND je.entryDate >= :fromDate
            AND je.entryDate <= :toDate
            """)
    List<JournalLineEntity> findByAccountIdAndUserIdAndEntryDateBetween(
            @Param("accountId") Long accountId,
            @Param("userId") Long userId,
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate
    );
}
