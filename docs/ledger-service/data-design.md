# Ledger service data design

**Purpose**: Describe how `ledger-service` data is stored and why it is structured this way.
<br>
Last updated: 2026-05-11

Back to: [Ledger service guide](../guide-ledger-service.md)

## Database and schema

- **Database**: PostgreSQL (shared across services in `docker-compose.yml`)
- **Schema**: `ledger`
- **Migrations**: Flyway under [`../../ledger-service/src/main/resources/db/migration/`](../../ledger-service/src/main/resources/db/migration/)

## Purpose of the data

The ledger schema is the system’s source of truth for accounting state:

- A stable chart of accounts (classifications → types → accounts)
- Journal entries and journal lines that represent posted transactions
- User-owned **budget plans** (what-if cash-flow modeling) stored alongside the chart, without posting to the journal
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
- `V9__Budget_planning.sql`: user-owned budget plans, incomes, recurring payables, budgets, and funding lines (see below)

## Tables

### Budget planning (`V9__Budget_planning.sql`)

These tables support **what-if** budgeting per user (not posted journal activity). Amounts are stored in **minor units**; recurring lines use `frequency_unit` in `days` / `weeks` / `months` / `years` with `frequency_number >= 1`. The application layer converts to **per-day** minor units using a fixed **30/365** basis (1 week = 7 days, 1 month = 30 days, 1 year = 365 days), with per-day division **half-up** to integer minor units. Derived per-week / per-month / per-year display amounts are computed from that per-day figure (×7, ×30, ×365) and are not persisted.

- **`budgeting_plans`**: `budget_plan_id`, `user_id`, `name`, `created_at`, `last_edited_at`. Index on `user_id`. Deleting a plan **CASCADE**s all child rows below.
- **`bp_incomes`**: recurring income lines; `budget_plan_id` → `budgeting_plans` **ON DELETE CASCADE**; `account_id` → `accounts` **ON DELETE RESTRICT** (must be a Revenue-classification account in application validation). Non-zero `amount_minor`.
- **`bp_recurring_payables`**: recurring payables; `budget_plan_id` **CASCADE**; `budget_account_id` → `accounts` **RESTRICT** (Budget-type account in validation). Non-zero `amount_minor`.
- **`bp_budgets`**: per-plan budget buckets with `target_amount_per_day_minor` (non-zero) and `account_id` → `accounts` **RESTRICT** (Budget-type account in validation); `budget_plan_id` **CASCADE**.
- **`bp_funding_lines`**: links a budget to an income with its own recurring amount/frequency; `bp_budget_id` → `bp_budgets` **CASCADE**; `bp_income_id` → `bp_incomes` **RESTRICT** (cannot delete an income row still referenced by a funding line).

Indexes: `budget_plan_id` on child plan tables; `bp_budget_id` and `bp_income_id` on `bp_funding_lines`.

## Core accounting tables

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
- Budget planning: `user_id` on `budgeting_plans`; `budget_plan_id` on `bp_incomes`, `bp_recurring_payables`, and `bp_budgets`; `bp_budget_id` / `bp_income_id` on `bp_funding_lines`

