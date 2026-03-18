package zachkingcade.dev.ledger.application.commands.journal;

public record JournalLineCommandObject(
        Long amount,
        Long accountId,
        char direction,
        String notes
) {
}
