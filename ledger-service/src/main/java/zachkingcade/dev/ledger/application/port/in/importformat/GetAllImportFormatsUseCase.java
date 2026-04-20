package zachkingcade.dev.ledger.application.port.in.importformat;

import zachkingcade.dev.ledger.domain.importformat.ImportFormat;

import java.util.List;

public interface GetAllImportFormatsUseCase {
    List<ImportFormat> getAllActiveImportFormats();
}

