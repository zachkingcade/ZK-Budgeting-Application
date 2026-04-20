package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.ImportFormatEntity;

import java.util.List;

public interface ImportFormatJpaRepository extends JpaRepository<ImportFormatEntity, Long> {
    List<ImportFormatEntity> findAllByActiveTrueOrderByFormatNameAsc();
}

