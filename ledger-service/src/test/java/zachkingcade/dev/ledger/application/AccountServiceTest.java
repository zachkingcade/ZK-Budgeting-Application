package zachkingcade.dev.ledger.application;

import org.junit.jupiter.api.Test;
import zachkingcade.dev.ledger.application.commands.account.CreateAccountCommand;
import zachkingcade.dev.ledger.application.commands.account.UpdateAccountCommand;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.port.out.account.AccountRepositoryPort;
import zachkingcade.dev.ledger.domain.account.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    private static class FakeAccountRepositoryPort implements AccountRepositoryPort {
        private final List<Account> accounts = new ArrayList<>();
        private Account findByIdResult;
        private Account findByDescriptionResult;
        private Boolean existsByDescriptionResult;

        private Account saved;
        private int saveCalls = 0;
        private Long nextId = 1L;

        void whenFindById(Account account) {
            this.findByIdResult = account;
        }

        void whenExistsByDescription(boolean exists) {
            this.existsByDescriptionResult = exists;
        }

        void whenFindByDescription(Account account) {
            this.findByDescriptionResult = account;
        }

        Account saved() {
            return saved;
        }

        int saveCalls() {
            return saveCalls;
        }

        @Override
        public List<Account> findAll() {
            return accounts;
        }

        @Override
        public Account findById(Long id) {
            return findByIdResult;
        }

        @Override
        public Account findByDescription(String description) {
            return findByDescriptionResult;
        }

        @Override
        public Boolean existsByDescription(String description) {
            return existsByDescriptionResult;
        }

        @Override
        public Account save(Account accountToSave) {
            saveCalls++;
            this.saved = accountToSave;
            return accountToSave.withId(nextId++);
        }
    }

    @Test
    void createAccount_notesMissing_defaultsToEmptyAndSaves() {
        FakeAccountRepositoryPort repo = new FakeAccountRepositoryPort();
        AccountService service = new AccountService(repo);

        CreateAccountCommand command = new CreateAccountCommand(
                10L,
                "Household Bills",
                Optional.empty()
        );

        Account result = service.createAccount(command);

        assertEquals(1L, result.id());
        assertEquals(10L, result.typeId());
        assertEquals("Household Bills", result.description());
        assertTrue(result.active());
        assertEquals("", result.notes());
        assertEquals(1, repo.saveCalls());
        assertNotNull(repo.saved());
    }

    @Test
    void updateAccount_uniqueDescriptionConflict_throwsApplicationException() {
        FakeAccountRepositoryPort repo = new FakeAccountRepositoryPort();

        // Current account in DB
        repo.whenFindById(Account.rehydrate(1L, 10L, "Old description", true, "old-notes"));

        // Unique description already exists for a different account id
        repo.whenExistsByDescription(true);
        repo.whenFindByDescription(Account.rehydrate(999L, 10L, "New description", true, "other"));

        AccountService service = new AccountService(repo);

        UpdateAccountCommand command = new UpdateAccountCommand(
                1L,
                Optional.of("New description"),
                Optional.empty(),
                Optional.empty()
        );

        ApplicationException ex = assertThrows(ApplicationException.class, () -> service.updateAccount(command));
        assertTrue(ex.getMessage().contains("already exists with the description"));
    }
}

