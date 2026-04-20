package zachkingcade.dev.ledger.application.port.out.importformat;

import zachkingcade.dev.ledger.domain.importformat.ImportFormat;

import java.util.List;
import java.util.Optional;

public interface ImportFormatRepositoryPort {
    List<ImportFormat> findAllActive();

    Optional<ImportFormat> findById(Long id);
}

