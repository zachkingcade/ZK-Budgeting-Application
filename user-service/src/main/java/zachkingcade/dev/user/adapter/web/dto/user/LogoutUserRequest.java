package zachkingcade.dev.user.adapter.web.dto.user;

public record LogoutUserRequest(
        String username,
        String sessionToken
) {
}
