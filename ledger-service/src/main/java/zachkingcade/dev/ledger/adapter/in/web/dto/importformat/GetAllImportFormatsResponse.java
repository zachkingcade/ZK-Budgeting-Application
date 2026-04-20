package zachkingcade.dev.ledger.adapter.in.web.dto.importformat;

import java.util.List;

public record GetAllImportFormatsResponse(
        List<ImportFormatObject> importFormats
) {}

