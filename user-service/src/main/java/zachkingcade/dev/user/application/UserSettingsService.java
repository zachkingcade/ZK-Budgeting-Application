package zachkingcade.dev.user.application;

import org.springframework.stereotype.Service;
import zachkingcade.dev.user.application.exception.ConflictException;
import zachkingcade.dev.user.application.exception.NotFoundException;
import zachkingcade.dev.user.application.port.in.usersetting.UserSettingsUseCase;
import zachkingcade.dev.user.application.port.out.usersetting.UserSettingRepositoryPort;
import zachkingcade.dev.user.domain.usersetting.UserSetting;

import java.time.Instant;
import java.util.List;

@Service
public class UserSettingsService implements UserSettingsUseCase {

    private final UserSettingRepositoryPort repository;

    public UserSettingsService(UserSettingRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<UserSetting> listAll(Long userId) {
        return repository.findAllByUserId(userId);
    }

    @Override
    public UserSetting getById(Long userId, Long settingId) {
        return repository.findByIdAndUserId(settingId, userId)
                .orElseThrow(() -> new NotFoundException("Setting not found"));
    }

    @Override
    public UserSetting getByName(Long userId, String settingName) {
        return repository.findByNameAndUserId(settingName, userId)
                .orElseThrow(() -> new NotFoundException("Setting not found"));
    }

    @Override
    public UserSetting create(Long userId, String settingName, String settingValue) {
        if (repository.existsByNameAndUserId(settingName, userId)) {
            throw new ConflictException("Setting name already exists for this user");
        }
        Instant now = Instant.now();
        UserSetting setting = new UserSetting(null, userId, settingName, settingValue, now, null);
        return repository.save(setting);
    }

    @Override
    public void deleteById(Long userId, Long settingId) {
        if (repository.findByIdAndUserId(settingId, userId).isEmpty()) {
            throw new NotFoundException("Setting not found");
        }
        repository.deleteByIdAndUserId(settingId, userId);
    }

    @Override
    public void deleteByName(Long userId, String settingName) {
        if (repository.findByNameAndUserId(settingName, userId).isEmpty()) {
            throw new NotFoundException("Setting not found");
        }
        repository.deleteByNameAndUserId(settingName, userId);
    }
}
