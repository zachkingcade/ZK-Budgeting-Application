package zachkingcade.dev.user.application.port.in.usersetting;

import zachkingcade.dev.user.domain.usersetting.UserSetting;

import java.util.List;

public interface UserSettingsUseCase {

    List<UserSetting> listAll(Long userId);

    UserSetting getById(Long userId, Long settingId);

    UserSetting getByName(Long userId, String settingName);

    UserSetting create(Long userId, String settingName, String settingValue);

    void deleteById(Long userId, Long settingId);

    void deleteByName(Long userId, String settingName);
}
