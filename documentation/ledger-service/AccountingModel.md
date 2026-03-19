# Ledger Service Accounting Model

## Model Type
The service implements a double-entry journal model using 5 main components:
- Account Classification
- Account Type
- Accounts
- Journal Entries

## Classification Semantics
Account classifications represent core parts of the accounting equation Assets = Equity - Liability.
An account classification denotes what part of the accounting equation an account type fits into, as such it also stores
weather a debit or credit decreases or increases a given account of the set type. Classifcations are created by the system, not the user, 
and cannot be changed.

## Account Type Creation Rules
- An Account type must describe it's purpose (Example: Debt)
- Must reference a classificaiton (Example: Liability)

## Account Creation Rules
- An Account must describe what it is responsible for representing (Example: Household Bills)
- Must reference an existing Account Type (Example: Spending Account)

## Journal Posting Rules
- A journal entry must have at least two lines.
- Total debit amount must equal total credit amount.
- Journal line amount must be positive.
- Journal line must reference an existing account.

## Validation Layers
- Domain model enforces core posting and structure rules (`JournalEntry`, `JournalLine`).
- Database constraints enforce valid direction and positive amount.
- Foreign keys enforce valid account/type/classification references.


- Create flow: command -> domain line creation -> domain journal creation -> repository save.
- Update flow: fetch existing entry -> merge requested line-note updates -> rehydrate domain -> save.
