package zachkingcade.dev.user.adapter.persistence;

import org.springframework.stereotype.Component;
import zachkingcade.dev.user.adapter.persistence.jpa.ServicePermissionEntity;
import zachkingcade.dev.user.adapter.persistence.repository.ServicePermissionJpaRepository;
import zachkingcade.dev.user.application.port.out.servicepermission.ServicePermissionRepositoryPort;
import zachkingcade.dev.user.domain.servicepermission.ServicePermission;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ServicePermissionPersistenceAdapter implements ServicePermissionRepositoryPort {

    private final ServicePermissionJpaRepository repository;

    public ServicePermissionPersistenceAdapter(ServicePermissionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ServicePermission> findByServiceName(String serviceName) {
        return repository.findByServiceName(serviceName).map(this::toDomain);
    }

    private ServicePermission toDomain(ServicePermissionEntity e) {
        return new ServicePermission(
                e.getId(),
                e.getServiceName(),
                e.getSecretHash(),
                splitCsv(e.getAllowedAudiences()),
                splitCsv(e.getAllowedScopes()),
                e.isMayActForUser(),
                e.getCreatedDate()
        );
    }

    private static List<String> splitCsv(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
