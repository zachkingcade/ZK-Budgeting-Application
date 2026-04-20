package zachkingcade.dev.ledger.adapter.in.web.dto.importformat;

public record ImportFormatObject(
        Long formatId,
        String formatName,
        String formatType,
        String formatDetails,
        boolean active
) {}

