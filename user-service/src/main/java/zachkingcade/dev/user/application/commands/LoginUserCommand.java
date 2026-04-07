package zachkingcade.dev.user.application.commands;

public record LoginUserCommand (
        String username,
        String password
) { }
