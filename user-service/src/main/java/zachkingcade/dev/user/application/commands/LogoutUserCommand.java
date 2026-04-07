package zachkingcade.dev.user.application.commands;

public record LogoutUserCommand(
        String username,
        String sessionToken
) {
}
