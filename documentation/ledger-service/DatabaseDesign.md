# Ledger Service Database Design

## Database and Schema
- Database: PostgreSQL.
- Schema: `ledger`.
- Migration mechanism: Flyway (`resources/db/migration/*`).

## Tables
- `account_classifications`
  - PK: `classifications_id`
  - Unique: `classification_description`
  - Check constraints: `credit_effect`, `debit_effect` in `+`/`-`
- `account_types`
  - PK: `type_id`
  - FK: `classification_id -> account_classifications.classifications_id`
  - Unique: `type_description`
  - Defaults: `notes=''`, `type_active=true`
- `accounts`
  - PK: `account_id`
  - FK: `account_type_id -> account_types.type_id`
  - Defaults: `notes=''`, `account_active=true`
- `journal_entries`
  - PK: `journal_entry_id`
  - Fields: `entry_date`, `description`, `notes`
- `journal_lines`
  - PK: `journal_line_id`
  - FK: `journal_entry_id -> journal_entries.journal_entry_id` (`ON DELETE CASCADE`)
  - FK: `account_id -> accounts.account_id`
  - Check constraints: `direction` in `D`/`C`, `amount > 0`

## Indexes
- `idx_account_types_classification_id`
- `idx_accounts_account_type`
- `idx_journal_lines_entry_id`
- `idx_journal_lines_account_id`

## JPA Mapping Notes
- Entities under `adapter/out/persistence/jpa` mirror table design.
- `JournalEntryEntity` owns a `@OneToMany` to `JournalLineEntity` with `cascade = ALL` and `orphanRemoval = true`.
- Repository graph loading in `JournalEntryJpaRepository` uses `@EntityGraph` for lines and account details allowing springboot to grab journal entry lines anytime
a journal entry is grabbed.
