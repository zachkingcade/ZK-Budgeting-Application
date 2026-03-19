# Ledger Service Use Cases

## Account Use Cases
- `GetAllAccountsUseCase`: list all accounts.
- `GetByIdAccountUseCase`: fetch account by id.
- `CreateAccountUseCase`: create account from type and description.
- `UpdateAccountUseCase`: update description, notes, and active flag.

Implemented by `application/AccountService`.

## Account Type Use Cases
- `GetAllAccountTypeUseCase`: list all account types.
- `GetByIdAccountTypeUseCase`: fetch account type by id.
- `CreateAccountTypeUseCase`: create type under a classification.
- `UpdateAccountTypeUseCase`: update description, notes, active flag.

Implemented by `application/AccountTypeService`.

## Account Classification Use Cases
- `GetAllAccountClassifcationsUseCase`: list classifications.
- `GetByIdAccountClassificaitonUseCase`: fetch classification by id.

Implemented by `application/AccountClassificationService`.

## Journal Entry Use Cases
- `GetAllJournalEntryUsecase`: list journal entries with lines.
- `GetByIdJournalEntryUseCase`: fetch journal entry by id.
- `CreateJournalEntryUseCase`: create journal entry with lines.
- `UpdateJournalEntryUsecase`: update header fields and line notes.

Implemented by `application/JournalEntryService`.

## Business Notes
- Journal creation and rehydration enforce double-entry balancing.
- Account and account type updates include uniqueness checks on description.
