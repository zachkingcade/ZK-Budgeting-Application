package zachkingcade.dev.user.application.port.in.user;

import zachkingcade.dev.user.application.commands.RegisterUserCommand;
import zachkingcade.dev.user.domain.user.User;

public interface RegisterUserUseCase {
    User registerUser(RegisterUserCommand command);
}
