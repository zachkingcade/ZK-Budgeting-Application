package zachkingcade.dev.user.adapter.web.dto.user;

public record RefreshLoginRequest(
        String username,
        String sessionToken
) {
}
