package zachkingcade.dev.user.application.port.in.service;

import zachkingcade.dev.user.application.commands.ServiceLoginCommand;
import zachkingcade.dev.user.application.results.ServiceLoginResult;

public interface ServiceLoginUseCase {

    ServiceLoginResult loginService(ServiceLoginCommand command);
}
