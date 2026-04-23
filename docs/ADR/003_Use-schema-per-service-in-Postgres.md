# ADR 0002: Use Schema-Per-Service in Postgres

## Context
The system consists of multiple microservices, each responsible for a distinct domain (e.g., ledger, user, reporting). A decision must be made on how to structure database ownership and isolation.

Options include:
- Shared database with shared tables
- Shared database with separate schemas
- Fully separate databases per service

The project aims to balance isolation with development simplicity.

## Decision
Each microservice will use its own dedicated schema within a shared Postgres instance.

Examples:
- `ledger` schema for ledger-service
- `user` schema for user-service
- `reporting` schema for reporting-service

Each service exclusively owns and manages its schema, including migrations via Flyway.

## Consequences

### Positive
- Clear ownership of data by each service.
- Prevents accidental cross-service table access.
- Easier local development compared to managing multiple database instances.
- Logical separation without requiring additional infrastructure.

### Negative
- Less isolation than fully separate databases.
- Requires discipline to avoid cross-schema queries.
- Potential for tighter coupling if boundaries are not respected.

## Alternatives Considered

### Shared Schema Across Services
All services use the same schema and tables.

- **Pros**: Simplest setup.
- **Cons**: High coupling, difficult to maintain service boundaries.

### Database Per Service
Each service has its own independent database instance.

- **Pros**: Strong isolation and true microservice independence.
- **Cons**: Increased operational complexity, harder local setup, more infrastructure overhead.