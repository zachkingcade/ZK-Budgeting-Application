# Ledger Service Domain Model

## Core Domain Types
- `Account`: chart-of-accounts leaf linked to an account type.
- `AccountType`: category linked to an account classification.
- `AccountClassification`: defines parts of the core accounting equation and their affects.
- `JournalEntry`: accounting entry header and aggregate root for lines.
- `JournalLine`: individual debit or credit posting line.

## Relationships
- `Account.typeId -> AccountType.id`
- `AccountType.classificationId -> AccountClassification.id`
- `JournalEntry` has many `JournalLine`
- `JournalLine.accountId -> Account.id`

## Invariants
### Account
- `typeId` is required.
- `description` must be non-null and non-empty.
- `notes` defaults to empty string when null on create.

### AccountType
- `classificationId` is required.
- `description` must be non-null and non-empty.

### AccountClassification
- `description` must be non-null and non-empty.
- `creditEffect` and `debitEffect` must be either `+` or `-`.

### JournalLine
- `amount` must be positive (`> 0`).
- `accountId` is required.
- `direction` must be either `C` or `D`.

### JournalEntry
- `entryDate` is required.
- `description` must be non-null and non-empty.
- at least 2 journal lines are required.
- sum of credits must equal sum of debits.

## Exception Model
- `DomainException` is thrown when domain invariants are violated.
- Application-level policies (for example, uniqueness checks) can throw `ApplicationException`.
