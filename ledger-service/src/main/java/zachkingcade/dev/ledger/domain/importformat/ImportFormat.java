package zachkingcade.dev.ledger.domain.importformat;

public record ImportFormat(
        Long id,
        String formatName,
        String formatType,
        String formatDetails,
        boolean active,
        String beanName
) {}

