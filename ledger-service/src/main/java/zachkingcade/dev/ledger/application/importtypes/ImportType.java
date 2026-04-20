package zachkingcade.dev.ledger.application.importtypes;

import java.io.InputStream;
import java.util.List;

public interface ImportType {
    List<PendingTransactionDraft> parse(InputStream inputStream, ImportFormatDetails details);
}

