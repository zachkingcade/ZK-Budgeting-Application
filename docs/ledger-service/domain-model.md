# Ledger service domain model

**Purpose**: Define the core accounting concepts and business rules implemented by `ledger-service`.
<br>
Last updated: 2026-04-22

Back to: [Ledger service guide](../guide-ledger-service.md)

## Mental model

The service implements a double-entry journal model:

- **AccountClassification**: semantic “bucket” in the accounting equation (and how debits/credits affect it)
- **AccountType**: user-defined category under a classification
- **Account**: leaf in the chart of accounts under an account type
- **JournalEntry**: aggregate root representing a posting event
- **JournalLine**: a debit/credit line against an account

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
- Classifications are system-defined and not user-editable.

### JournalLine

- `amount` must be positive (\(> 0\)).
- `accountId` is required.
- `direction` must be either `C` or `D`.

### JournalEntry

- `entryDate` is required.
- `description` must be non-null and non-empty.
- At least 2 journal lines are required.
- Sum of credits must equal sum of debits.

## Validation layers

- **Domain layer** enforces invariants when constructing/rehydrating domain objects.
- **Database constraints** enforce direction and positivity.
- **Foreign keys** enforce valid references (account/type/classification).

