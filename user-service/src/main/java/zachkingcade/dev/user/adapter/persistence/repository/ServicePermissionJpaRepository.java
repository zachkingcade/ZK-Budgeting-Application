package zachkingcade.dev.user.adapter.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zachkingcade.dev.user.adapter.persistence.jpa.ServicePermissionEntity;

import java.util.Optional;

public interface ServicePermissionJpaRepository extends JpaRepository<ServicePermissionEntity, Long> {

    Optional<ServicePermissionEntity> findByServiceName(String serviceName);
}
