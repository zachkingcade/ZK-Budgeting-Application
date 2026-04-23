# Ledger service data design

**Purpose**: Describe how `ledger-service` data is stored and why it is structured this way.
<br>
Last updated: 2026-04-22

Back to: [Ledger service guide](../guide-ledger-service.md)

## Database and schema

- **Database**: PostgreSQL (shared across services in `docker-compose.yml`)
- **Schema**: `ledger`
- **Migrations**: Flyway under [`../../ledger-service/src/main/resources/db/migration/`](../../ledger-service/src/main/resources/db/migration/)

## Purpose of the data

The ledger schema is the system’s source of truth for accounting state:

- A stable chart of accounts (classifications → types → accounts)
- Journal entries and journal lines that represent posted transactions
- Constraints that ensure data remains valid and auditable over time

## Flyway usage

Flyway is used to evolve the `ledger` schema over time through versioned, append-only migrations. This gives a reproducible schema history across environments and keeps database changes reviewable in git.

See ADR: [`../ADR/004_Use-Flyway-for-schema-management.md`](../ADR/004_Use-Flyway-for-schema-management.md)

## Migration history

Migrations live in [`../../ledger-service/src/main/resources/db/migration/`](../../ledger-service/src/main/resources/db/migration/). Highlights:

- `V1__Init.sql`: baseline schema for ledger entities
- `V2__User_Update.sql`: user ownership wiring
- `V3__Account_Classificaitons.sql`: classification seed/structure
- `V4__System_Account_Types.sql`: system-owned account types
- `V5__User_Description_Uniqueness_Updates.sql`: tighten uniqueness semantics
- `V6__Pending_transactions_and_import_formats.sql`: pending transactions + import formats
- `V7__Update_USAA_import_date_format.sql`: import format adjustments

## Tables

- `account_classifications`
  - Uniqueness on `classification_description`
  - Check constraints: `credit_effect`, `debit_effect` in `+`/`-`
- `account_types`
  - FK: `classification_id -> account_classifications.classifications_id`
  - Uniqueness on `type_description`
- `accounts`
  - FK: `account_type_id -> account_types.type_id`
- `journal_entries`
  - Entry header (`entry_date`, `description`, `notes`)
- `journal_lines`
  - FK: `journal_entry_id -> journal_entries.journal_entry_id` (`ON DELETE CASCADE`)
  - FK: `account_id -> accounts.account_id`
  - Check constraints: `direction` in `D`/`C`, `amount > 0`

## Indexing

Indexes exist to make common joins fast:

- Type/classification relationships
- Account/type relationships
- Line-to-entry and line-to-account lookups

