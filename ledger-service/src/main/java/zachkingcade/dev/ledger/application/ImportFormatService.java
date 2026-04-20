package zachkingcade.dev.ledger.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import zachkingcade.dev.ledger.application.port.in.importformat.GetAllImportFormatsUseCase;
import zachkingcade.dev.ledger.application.port.out.importformat.ImportFormatRepositoryPort;
import zachkingcade.dev.ledger.domain.importformat.ImportFormat;

import java.util.List;

@Service
public class ImportFormatService implements GetAllImportFormatsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ImportFormatService.class);

    private final ImportFormatRepositoryPort importFormatRepository;

    public ImportFormatService(ImportFormatRepositoryPort importFormatRepository) {
        this.importFormatRepository = importFormatRepository;
    }

    @Override
    public List<ImportFormat> getAllActiveImportFormats() {
        try {
            log.debug("Starting Get All Import Formats");
            List<ImportFormat> results = importFormatRepository.findAllActive();
            log.debug("Ending Get All Import Formats results:[{}]", results.size());
            return results;
        } catch (RuntimeException ex) {
            log.error("ImportFormatService.getAllActiveImportFormats failed", ex);
            throw ex;
        }
    }
}

