package zachkingcade.dev.ledger.adapter.out.persistence;

import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.ImportFormatEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.ImportFormatJpaRepository;
import zachkingcade.dev.ledger.application.port.out.importformat.ImportFormatRepositoryPort;
import zachkingcade.dev.ledger.domain.importformat.ImportFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ImportFormatPersistenceAdapter implements ImportFormatRepositoryPort {

    private final ImportFormatJpaRepository importFormatJpaRepository;

    public ImportFormatPersistenceAdapter(ImportFormatJpaRepository importFormatJpaRepository) {
        this.importFormatJpaRepository = importFormatJpaRepository;
    }

    @Override
    public List<ImportFormat> findAllActive() {
        List<ImportFormatEntity> entities = importFormatJpaRepository.findAllByActiveTrueOrderByFormatNameAsc();
        List<ImportFormat> results = new ArrayList<>();
        for (ImportFormatEntity e : entities) {
            results.add(mapToDomain(e));
        }
        return results;
    }

    @Override
    public Optional<ImportFormat> findById(Long id) {
        return importFormatJpaRepository.findById(id).map(this::mapToDomain);
    }

    private ImportFormat mapToDomain(ImportFormatEntity e) {
        return new ImportFormat(
                e.getId(),
                e.getFormatName(),
                e.getFormatType(),
                e.getFormatDetails(),
                Boolean.TRUE.equals(e.getActive()),
                e.getBeanName()
        );
    }
}

