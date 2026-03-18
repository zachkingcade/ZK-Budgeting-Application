package zachkingcade.dev.ledger.application.commands.journal;

public record JournalLineUpdateCommandObject(
        Long id,
        String notes
) {
}
