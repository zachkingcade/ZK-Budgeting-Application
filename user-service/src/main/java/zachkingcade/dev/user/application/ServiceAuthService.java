package zachkingcade.dev.user.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import zachkingcade.dev.user.application.commands.ServiceLoginCommand;
import zachkingcade.dev.user.application.exception.ApplicationException;
import zachkingcade.dev.user.application.exception.NotFoundException;
import zachkingcade.dev.user.application.port.in.service.ServiceLoginUseCase;
import zachkingcade.dev.user.application.port.out.servicepermission.ServicePermissionRepositoryPort;
import zachkingcade.dev.user.application.results.ServiceLoginResult;
import zachkingcade.dev.user.domain.servicepermission.ServicePermission;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ServiceAuthService implements ServiceLoginUseCase {

    private static final long SERVICE_TOKEN_SECONDS = 900L;

    private final ServicePermissionRepositoryPort servicePermissionRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    public ServiceAuthService(
            ServicePermissionRepositoryPort servicePermissionRepositoryPort,
            PasswordEncoder passwordEncoder,
            JWTService jwtService
    ) {
        this.servicePermissionRepositoryPort = servicePermissionRepositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public ServiceLoginResult loginService(ServiceLoginCommand command) {
        ServicePermission row = servicePermissionRepositoryPort.findByServiceName(command.serviceName())
                .orElseThrow(() -> new NotFoundException(String.format("Service [%s] not found", command.serviceName())));

        if (!passwordEncoder.matches(command.secret(), row.secretHash())) {
            throw new ApplicationException("Invalid service credentials");
        }

        if (command.actingForUserId() != null && !row.mayActForUser()) {
            throw new ApplicationException("This service is not permitted to act for a user");
        }

        List<String> effectiveAudiences = resolveIntersection(command.requestedAudiences(), row.allowedAudiences(), "audience");
        List<String> effectiveScopes = resolveIntersection(command.requestedScopes(), row.allowedScopes(), "scope");

        Instant created = Instant.now();
        Instant expires = created.plus(SERVICE_TOKEN_SECONDS, ChronoUnit.SECONDS);
        String token = jwtService.generateServiceAccessToken(
                command.serviceName(),
                command.actingForUserId(),
                effectiveAudiences,
                effectiveScopes,
                created,
                expires
        );
        return new ServiceLoginResult(token, created, expires);
    }

    private static List<String> resolveIntersection(List<String> requested, List<String> allowed, String label) {
        if (allowed == null || allowed.isEmpty()) {
            throw new ApplicationException("Service permission row has no allowed " + label + "s configured");
        }
        if (requested == null || requested.isEmpty()) {
            return List.copyOf(allowed);
        }
        Set<String> allow = new LinkedHashSet<>(allowed);
        List<String> out = requested.stream().map(String::trim).filter(s -> !s.isEmpty())
                .filter(allow::contains)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
        if (out.isEmpty()) {
            throw new ApplicationException("Requested " + label + "s are not permitted for this service");
        }
        return out;
    }
}
