package zachkingcade.dev.ledger.application.commands.journal;

public record RemoveByIdJournalEntryCommand(
        Long userId,
        long id
) {
}
