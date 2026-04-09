package zachkingcade.dev.ledger.application.commands.accounttype;

public record GetByIdAccountTypeCommand(
        Long userId,
        Long id
) {
}
