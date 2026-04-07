package zachkingcade.dev.user.application.commands;

public record RefreshSessionCommand(
        String username,
        String sessionToken
) {
}
