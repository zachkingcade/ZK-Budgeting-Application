package zachkingcade.dev.user.application.port.in.Session;

import zachkingcade.dev.user.application.commands.RefreshSessionCommand;
import zachkingcade.dev.user.application.results.RefreshSessionResult;

public interface RefreshSessionUseCase {
    RefreshSessionResult refreshSession(RefreshSessionCommand command);
}
