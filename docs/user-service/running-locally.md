Purpose: Document how to run `user-service` locally for development.
Last updated: 2026-04-22

## Prerequisites

- Java 17
- Docker + Docker Compose

## Start dependencies

From repository root:

```bash
docker compose up -d postgres
```

This starts PostgreSQL (container `ledger-postgres`) on `localhost:5432` and runs initialization scripts from `./db/init`.

## Run the service

From `user-service/`:

```bash
./mvnw spring-boot:run
```

Default service port: `8082`.

## Tests

```bash
./mvnw test
```

