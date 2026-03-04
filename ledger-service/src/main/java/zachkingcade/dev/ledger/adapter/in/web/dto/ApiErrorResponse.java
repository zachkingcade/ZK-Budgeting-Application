package zachkingcade.dev.ledger.adapter.in.web.dto;

public record ApiErrorResponse(
        String errorCode,
        String message
) {}
