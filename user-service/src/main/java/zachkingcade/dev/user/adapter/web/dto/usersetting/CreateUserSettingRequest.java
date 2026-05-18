package zachkingcade.dev.user.adapter.web.dto.usersetting;

import jakarta.validation.constraints.NotBlank;

public record CreateUserSettingRequest(
        @NotBlank String settingName,
        @NotBlank String settingValue
) {
}
