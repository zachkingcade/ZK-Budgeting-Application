package zachkingcade.dev.user.application.port.in.Session;

import zachkingcade.dev.user.application.commands.CreateSessionCommand;
import zachkingcade.dev.user.domain.session.Session;

public interface CreateUserSessionUseCase {
    Session createSession(CreateSessionCommand command);
}
