package zachkingcade.dev.user.adapter.persistence;

import org.springframework.stereotype.Service;
import zachkingcade.dev.user.adapter.persistence.jpa.UserSettingEntity;
import zachkingcade.dev.user.adapter.persistence.repository.UserSettingJpaRepository;
import zachkingcade.dev.user.application.port.out.usersetting.UserSettingRepositoryPort;
import zachkingcade.dev.user.domain.usersetting.UserSetting;

import java.util.List;
import java.util.Optional;

@Service
public class UserSettingPersistenceAdapter implements UserSettingRepositoryPort {

    private final UserSettingJpaRepository repository;

    public UserSettingPersistenceAdapter(UserSettingJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<UserSetting> findAllByUserId(Long userId) {
        return repository.findAllByUserIdOrderBySettingNameAsc(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<UserSetting> findByIdAndUserId(Long settingId, Long userId) {
        return repository.findBySettingIdAndUserId(settingId, userId).map(this::toDomain);
    }

    @Override
    public Optional<UserSetting> findByNameAndUserId(String settingName, Long userId) {
        return repository.findByUserIdAndSettingName(userId, settingName).map(this::toDomain);
    }

    @Override
    public boolean existsByNameAndUserId(String settingName, Long userId) {
        return repository.existsByUserIdAndSettingName(userId, settingName);
    }

    @Override
    public UserSetting save(UserSetting setting) {
        UserSettingEntity entity = new UserSettingEntity();
        if (setting.settingId() != null) {
            entity.setSettingId(setting.settingId());
        }
        entity.setUserId(setting.userId());
        entity.setSettingName(setting.settingName());
        entity.setSettingValue(setting.settingValue());
        entity.setCreatedDate(setting.createdDate());
        entity.setUpdatedDate(setting.updatedDate());
        return toDomain(repository.save(entity));
    }

    @Override
    public void deleteByIdAndUserId(Long settingId, Long userId) {
        repository.deleteBySettingIdAndUserId(settingId, userId);
    }

    @Override
    public void deleteByNameAndUserId(String settingName, Long userId) {
        repository.deleteByUserIdAndSettingName(userId, settingName);
    }

    private UserSetting toDomain(UserSettingEntity entity) {
        return new UserSetting(
                entity.getSettingId(),
                entity.getUserId(),
                entity.getSettingName(),
                entity.getSettingValue(),
                entity.getCreatedDate(),
                entity.getUpdatedDate()
        );
    }
}
