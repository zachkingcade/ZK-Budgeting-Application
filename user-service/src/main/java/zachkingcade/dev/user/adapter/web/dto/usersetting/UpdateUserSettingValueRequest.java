package zachkingcade.dev.user.adapter.web.dto.usersetting;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserSettingValueRequest(
        @NotBlank String settingValue
) {
}
