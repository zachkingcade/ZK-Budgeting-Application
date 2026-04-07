package zachkingcade.dev.user.adapter.web.dto;

public record ApiErrorResponse(
        String errorCode,
        String message
) {}
