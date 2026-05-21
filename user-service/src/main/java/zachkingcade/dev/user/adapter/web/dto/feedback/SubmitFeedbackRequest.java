package zachkingcade.dev.user.adapter.web.dto.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SubmitFeedbackRequest(
        @NotBlank
        @Pattern(regexp = "bug|suggestion")
        String type,
        @NotBlank
        String title,
        @NotBlank
        String message
) {
}
