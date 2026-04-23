# Ledger service project structure

**Purpose**: Explain where code lives in `ledger-service` so contributors can navigate quickly.
<br>
Last updated: 2026-04-22

Back to: [Ledger service guide](../guide-ledger-service.md)

## High-level layout

Base package: `ledger-service/src/main/java/zachkingcade/dev/ledger`

Key entrypoints:

- Controllers: [`../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/)
- Web DTOs: [`../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/`](../../ledger-service/src/main/java/zachkingcade/dev/ledger/adapter/in/web/dto/)
- Flyway migrations: [`../../ledger-service/src/main/resources/db/migration/`](../../ledger-service/src/main/resources/db/migration/)
- Runtime config: [`../../ledger-service/src/main/resources/application.yml`](../../ledger-service/src/main/resources/application.yml)

- `adapter/in/web`: REST controllers and web-facing DTO mapping
- `adapter/in/web/dto`: request/response DTOs
- `application`: use-case implementations and orchestration
- `application/commands`: immutable command records passed into use cases
- `application/port/in`: inbound use-case interfaces
- `application/port/out`: outbound persistence interfaces (ports)
- `domain`: core business model and domain exceptions (pure business rules)
- `adapter/out/persistence`: persistence adapters implementing outbound ports
- `adapter/out/persistence/jpa`: JPA entities
- `adapter/out/persistence/repository`: Spring Data JPA repositories

## Resources

- `src/main/resources/application.yml`: runtime config
- `src/main/resources/logback-spring.xml`: logging config
- `src/main/resources/db/migration/*`: Flyway migrations

## Root-level supporting files

- `pom.xml`: Maven build
- `mvnw`, `mvnw.cmd`: Maven wrapper
- `HELP.md`: Spring-generated help (non-canonical)

