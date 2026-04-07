package zachkingcade.dev.user.application.port.in.user;

import zachkingcade.dev.user.application.commands.LogoutUserCommand;

public interface LogoutUserUseCase {
    void logoutUser(LogoutUserCommand command);
}
