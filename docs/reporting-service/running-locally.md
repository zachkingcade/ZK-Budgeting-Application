Purpose: Document how to run `reporting-service` locally for development.
Last updated: 2026-04-22

## Prerequisites

- Java 17
- Docker + Docker Compose

## Start dependencies

From repository root:

```bash
docker compose up -d postgres user-service ledger-service
```

## Run the service

From `reporting-service/`:

```bash
./mvnw spring-boot:run
```

Default service port: `8083`.

## Tests

```bash
./mvnw test
```

