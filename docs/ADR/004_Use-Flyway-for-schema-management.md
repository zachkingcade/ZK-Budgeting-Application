# ADR 0009: Use Flyway for Schema Management

## Context
The system relies on a relational database (Postgres) with evolving schemas across multiple services.

Database changes must be:
- Version-controlled
- Reproducible across environments
- Applied consistently during deployment

A decision was required on how to manage database schema evolution.

## Decision
Flyway will be used for database schema management.

- Each service maintains its own migration scripts.
- Migrations are versioned and stored in the codebase.
- Migrations run automatically on application startup.
- Each service applies migrations only to its own schema.

## Consequences

### Positive
- Ensures consistent schema across environments.
- Provides a clear history of database changes.
- Enables safe, incremental schema evolution.
- Integrates cleanly with CI/CD pipelines.
- Industry Standard
- One of the Spring Boot Initializer Options

### Negative
- Requires discipline to manage migration scripts correctly.
- Mistakes in migrations can impact multiple environments.
- Rollbacks require explicit handling.