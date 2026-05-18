package zachkingcade.dev.user.adapter.web.dto.usersetting;

import zachkingcade.dev.user.domain.usersetting.UserSetting;

public record UserSettingResponse(
        Long settingId,
        String settingName,
        String settingValue
) {
    public static UserSettingResponse from(UserSetting setting) {
        return new UserSettingResponse(setting.settingId(), setting.settingName(), setting.settingValue());
    }
}
