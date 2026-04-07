package zachkingcade.dev.user.adapter.persistence;

import org.springframework.stereotype.Service;
import zachkingcade.dev.user.adapter.persistence.jpa.UserEntity;
import zachkingcade.dev.user.adapter.persistence.repository.UserJpaRepository;
import zachkingcade.dev.user.application.port.out.user.UserRepositoryPort;

import java.util.Optional;

@Service
public class UserPersistenceAdapter implements UserRepositoryPort {

    UserJpaRepository userRepository;

    public UserPersistenceAdapter(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public UserEntity save(UserEntity entityToSave) {
        return userRepository.save(entityToSave);
    }

    @Override
    public Optional<UserEntity> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
