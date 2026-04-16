package zachkingcade.dev.ledger.application;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import zachkingcade.dev.ledger.application.commands.accounttype.UpdateAccountTypeCommand;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.port.out.accounttype.AccountClassificationRepositoryPort;
import zachkingcade.dev.ledger.application.port.out.accounttype.AccountTypeRepositoryPort;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountTypeEntity;
import zachkingcade.dev.ledger.domain.account.AccountClassification;
import zachkingcade.dev.ledger.domain.account.AccountType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AccountTypeServiceTest {

    private static class FakeAccountTypeRepositoryPort implements AccountTypeRepositoryPort {
        private AccountType findByIdResult;
        private AccountType findByDescriptionResult;
        private Boolean existsByDescriptionResult;

        private int saveCalls = 0;

        void whenFindById(AccountType accountType) {
            this.findByIdResult = accountType;
        }

        void whenExistsByDescription(boolean exists) {
            this.existsByDescriptionResult = exists;
        }

        void whenFindByDescription(AccountType accountType) {
            this.findByDescriptionResult = accountType;
        }

        int saveCalls() {
            return saveCalls;
        }

        @Override
        public AccountType findByIdVisibleToUser(Long userId, Long id) {
            return findByIdResult;
        }

        @Override
        public List<AccountType> findAllVisibleToUser(Long userId) {
            return List.of();
        }

        @Override
        public List<AccountType> findAllVisibleToUser(Long userId, Sort sort) {
            return List.of();
        }

        @Override
        public List<AccountType> findAllVisibleToUser(Long userId, Specification<AccountTypeEntity> spec) {
            return List.of();
        }

        @Override
        public List<AccountType> findAllVisibleToUser(Long userId, Specification<AccountTypeEntity> spec, Sort sort) {
            return List.of();
        }

        @Override
        public AccountType findByDescription(String description) {
            return findByDescriptionResult;
        }

        @Override
        public Boolean existsByDescription(String description) {
            return existsByDescriptionResult;
        }

        @Override
        public AccountType save(AccountType accountTypeToSave) {
            saveCalls++;
            return accountTypeToSave.withId(1L);
        }
    }

    private static class FakeAccountClassificationRepositoryPort implements AccountClassificationRepositoryPort {
        @Override
        public AccountClassification findById(Long id) {
            return null;
        }

        @Override
        public List<AccountClassification> findAll() {
            return new ArrayList<>();
        }
    }

    @Test
    void updateAccountType_uniqueDescriptionConflict_throwsApplicationException() {
        FakeAccountTypeRepositoryPort accountTypeRepo = new FakeAccountTypeRepositoryPort();
        FakeAccountClassificationRepositoryPort classificationRepo = new FakeAccountClassificationRepositoryPort();

        accountTypeRepo.whenFindById(AccountType.rehydrate(1L, "Old type", 10L, "notes", true, 1L, false));
        accountTypeRepo.whenExistsByDescription(true);
        accountTypeRepo.whenFindByDescription(AccountType.rehydrate(999L, "New type", 10L, "other", true, 1L, false));

        AccountTypeService service = new AccountTypeService(accountTypeRepo, classificationRepo);

        UpdateAccountTypeCommand command = new UpdateAccountTypeCommand(
                1L,
                1L,
                Optional.of("New type"),
                Optional.empty(),
                Optional.empty()
        );

        ApplicationException ex = assertThrows(ApplicationException.class, () -> service.updateAccountType(command));
        assertTrue(ex.getMessage().contains("already exists with the description"));
        assertEquals(0, accountTypeRepo.saveCalls());
    }
}

