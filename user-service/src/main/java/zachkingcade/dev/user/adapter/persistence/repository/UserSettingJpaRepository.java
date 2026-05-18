package zachkingcade.dev.user.adapter.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.user.adapter.persistence.jpa.UserSettingEntity;

import java.util.List;
import java.util.Optional;

public interface UserSettingJpaRepository extends JpaRepository<UserSettingEntity, Long> {

    List<UserSettingEntity> findAllByUserIdOrderBySettingNameAsc(Long userId);

    Optional<UserSettingEntity> findBySettingIdAndUserId(Long settingId, Long userId);

    Optional<UserSettingEntity> findByUserIdAndSettingName(Long userId, String settingName);

    boolean existsByUserIdAndSettingName(Long userId, String settingName);

    void deleteBySettingIdAndUserId(Long settingId, Long userId);

    void deleteByUserIdAndSettingName(Long userId, String settingName);
}
