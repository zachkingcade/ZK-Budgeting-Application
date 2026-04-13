package zachkingcade.dev.user.application.port.out.servicepermission;

import zachkingcade.dev.user.domain.servicepermission.ServicePermission;

import java.util.Optional;

public interface ServicePermissionRepositoryPort {

    Optional<ServicePermission> findByServiceName(String serviceName);
}
