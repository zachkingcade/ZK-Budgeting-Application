# Ledger Service Ports and Adapters

## Inbound Adapters (HTTP)
- `AccountController` (`/accounts`)
- `AccountTypeController` (`/accounttypes`)
- `AccountClassificationController` (`/accountclassifications`)
- `JournalEntryController` (`/journalentry`)

## Inbound Ports (Use Cases)
- Accounts: `CreateAccountUseCase`, `GetAllAccountsUseCase`, `GetByIdAccountUseCase`, `UpdateAccountUseCase`
- Account Types: `CreateAccountTypeUseCase`, `GetAllAccountTypeUseCase`, `GetByIdAccountTypeUseCase`, `UpdateAccountTypeUseCase`
- Account Classifications: `GetAllAccountClassifcationsUseCase`, `GetByIdAccountClassificaitonUseCase`
- Journal Entries: `CreateJournalEntryUseCase`, `GetAllJournalEntryUsecase`, `GetByIdJournalEntryUseCase`, `UpdateJournalEntryUsecase`

## Application Services (Use-Case Implementations)
- `AccountService`
- `AccountTypeService`
- `AccountClassificationService`
- `JournalEntryService`

## Outbound Ports
- `AccountRepositoryPort`
- `AccountTypeRepositoryPort`
- `AccountClassificationRepositoryPort`
- `JournalEntryRepositoryPort`

## Outbound Adapters (Persistence)
- `AccountPersistenceAdapter` -> uses `AccountJpaRepository`
- `AccountTypePersistenceAdapter` -> uses `AccountTypeJpaRepository`
- `AccountTypeClassificationPersistenceAdapter` -> uses `AccountClassificationJpaRepository`
- `JournalEntryPersistenceAdapter` -> uses `JournalEntryJpaRepository` and `JournalLinesJpaRepository`

## Why This Matters
- Core business logic is isolated from transport and storage concerns.
- Ports make adapters replaceable and improve testability.
