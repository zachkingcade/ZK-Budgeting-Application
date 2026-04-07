package zachkingcade.dev.user.application.port.in.user;

import zachkingcade.dev.user.application.commands.LoginUserCommand;
import zachkingcade.dev.user.application.results.LogInUserResult;
import zachkingcade.dev.user.domain.session.Session;

public interface LoginUserUseCase {
    LogInUserResult loginUser(LoginUserCommand command);
}
