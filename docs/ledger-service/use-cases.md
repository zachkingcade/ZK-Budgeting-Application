Purpose: Describe key workflows supported by `ledger-service` and how parts interact.
Last updated: 2026-04-22

## Account use cases

- `GetAllAccountsUseCase`: list all accounts
- `GetByIdAccountUseCase`: fetch account by id
- `CreateAccountUseCase`: create an account from type + description
- `UpdateAccountUseCase`: update description, notes, and active flag

## Account type use cases

- `GetAllAccountTypeUseCase`: list all account types
- `GetByIdAccountTypeUseCase`: fetch account type by id
- `CreateAccountTypeUseCase`: create type under a classification
- `UpdateAccountTypeUseCase`: update description, notes, active flag

## Account classification use cases

- `GetAllAccountClassifcationsUseCase`: list classifications
- `GetByIdAccountClassificaitonUseCase`: fetch classification by id

## Journal entry use cases

- `GetAllJournalEntryUsecase`: list journal entries with lines
- `GetByIdJournalEntryUseCase`: fetch journal entry by id
- `CreateJournalEntryUseCase`: create a journal entry with lines
- `UpdateJournalEntryUsecase`: update header fields and line notes

## Business notes

- Journal creation and rehydration enforce double-entry balancing.

