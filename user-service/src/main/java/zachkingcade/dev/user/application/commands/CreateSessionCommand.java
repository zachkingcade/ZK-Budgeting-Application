package zachkingcade.dev.user.application.commands;

import zachkingcade.dev.user.adapter.persistence.jpa.UserEntity;
import zachkingcade.dev.user.domain.user.User;

import java.time.Instant;

public record CreateSessionCommand(
        UserEntity user
) {
}
