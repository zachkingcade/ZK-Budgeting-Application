# User service data design

**Purpose**: Describe how `user-service` stores identity/session data in Postgres.
<br>
Last updated: 2026-04-22

Back to: [User service guide](../guide-user-service.md)

## Database and schema

- **Database**: PostgreSQL (shared in local dev via `docker-compose.yml`)
- **Schema**: `auth` (see `user-service/src/main/resources/application.yml`)
- **Migrations**: Flyway under [`../../user-service/src/main/resources/db/migration/`](../../user-service/src/main/resources/db/migration/)

## Purpose of the data

The `auth` schema exists to support security and identity workflows:

- Persisted users (unique identity and credential hash)
- Persisted sessions (session token + expiration) for refresh/logout flows
- Service permissions that bound what internal services can request on behalf of users

## Flyway usage

Flyway migrations provide an auditable history of security-sensitive schema changes and seeded permissions.

See ADR: [`../ADR/004_Use-Flyway-for-schema-management.md`](../ADR/004_Use-Flyway-for-schema-management.md)

## Migration history

Migrations live in [`../../user-service/src/main/resources/db/migration/`](../../user-service/src/main/resources/db/migration/). Highlights:

- `V1__Init.sql`: users + sessions baseline
- `V2__Service_Permissions.sql`: service permissions table + initial seed
- `V3__Fix_reporting_service_secret_hash.sql`: secret hash fix/rotation for `reporting-service`

## Tables

### `users`

Source: `V1__Init.sql`

- `user_id` (PK)
- `username` (unique)
- `password_hash`
- `active`
- `created_date`, `updated_date`

### `user_sessions`

Source: `V1__Init.sql`

- `session_id` (PK)
- `user_id` (FK → `users.user_id`)
- `session_token` (unique)
- `created_date`
- `expires_date`

Indexes include `user_id` and `expires_date` for lookup/expiry queries.

### `service_permissions`

Source: `V2__Service_Permissions.sql`

Stores internal-service authentication and authorization boundaries:

- `service_name` (unique)
- `secret_hash` (BCrypt)
- `allowed_audiences` (string list)
- `allowed_scopes` (string list)
- `may_act_for_user` (boolean)

`reporting-service` is seeded by Flyway for local/dev flows and can be rotated via future migrations (see `V3__Fix_reporting_service_secret_hash.sql`).

