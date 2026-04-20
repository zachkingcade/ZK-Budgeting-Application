package zachkingcade.dev.ledger.application.importtypes;

import java.util.List;

public record ImportFormatDetails(
        String dateFormat,
        List<String> headerArray
) {}

