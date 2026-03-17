package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.JournalLineEntity;

public interface JournalLinesJpaRepository extends JpaRepository<JournalLineEntity, Long> {
}
