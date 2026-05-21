package zachkingcade.dev.user.application;

import org.springframework.stereotype.Service;
import zachkingcade.dev.user.adapter.persistence.repository.UserRoleJpaRepository;

import java.util.List;

@Service
public class RolesService {

    public static final String ADMIN_ROLE = "admin";

    private final UserRoleJpaRepository userRoleRepository;

    public RolesService(UserRoleJpaRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    public List<String> getRolesForUser(long userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(entity -> entity.getRoleName())
                .toList();
    }

    public boolean hasRole(long userId, String roleName) {
        return userRoleRepository.existsByUserIdAndRoleName(userId, roleName);
    }

    public boolean isAdmin(long userId) {
        return hasRole(userId, ADMIN_ROLE);
    }
}
