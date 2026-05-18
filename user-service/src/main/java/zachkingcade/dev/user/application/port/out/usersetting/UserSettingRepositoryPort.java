package zachkingcade.dev.user.application.port.out.usersetting;

import zachkingcade.dev.user.domain.usersetting.UserSetting;

import java.util.List;
import java.util.Optional;

public interface UserSettingRepositoryPort {

    List<UserSetting> findAllByUserId(Long userId);

    Optional<UserSetting> findByIdAndUserId(Long settingId, Long userId);

    Optional<UserSetting> findByNameAndUserId(String settingName, Long userId);

    boolean existsByNameAndUserId(String settingName, Long userId);

    UserSetting save(UserSetting setting);

    void deleteByIdAndUserId(Long settingId, Long userId);

    void deleteByNameAndUserId(String settingName, Long userId);
}
