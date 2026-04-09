package zachkingcade.dev.ledger.application.commands.journal;

public record GetByIdJournalEntryCommand (
        Long userId,
        Long id
){ }
