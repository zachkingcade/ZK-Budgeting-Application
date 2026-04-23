Purpose: Document how to run `ledger-service` locally for development.
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

From `ledger-service/`:

```bash
./mvnw spring-boot:run
```

Default service port: `8081`.

## Verify basic startup

```bash
curl http://localhost:8081/accounts/all
```

## Tests

```bash
./mvnw test
```

