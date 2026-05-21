package zachkingcade.dev.user.adapter.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.user.adapter.persistence.jpa.UserRoleEntity;

import java.util.List;

public interface UserRoleJpaRepository extends JpaRepository<UserRoleEntity, Long> {

    List<UserRoleEntity> findByUserId(Long userId);

    boolean existsByUserIdAndRoleName(Long userId, String roleName);
}
