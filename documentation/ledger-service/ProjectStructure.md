# Ledger Service Project Structure

## High-Level Layout
- `src/main/java/zachkingcade/dev/ledger`
  - `adapter/in/web`: REST controllers and API DTO mapping.
  - `adapter/in/web/dto`: request and response records.
  - `application`: use-case services.
  - `application/commands`: immutable command records passed into use cases.
  - `application/port/in`: inbound use-case interfaces.
  - `application/port/out`: outbound persistence interfaces.
  - `domain`: core business model and domain exceptions.
  - `adapter/out/persistence`: persistence adapters implementing outbound ports.
  - `adapter/out/persistence/jpa`: JPA entities.
  - `adapter/out/persistence/repository`: Spring Data JPA repositories.

## Resources
- `src/main/resources/application.yml`: service runtime config.
- `src/main/resources/logback-spring.xml`: logging configuration.
- `src/main/resources/db/migration/*`: schema migration.

## Root-Level Supporting Files
- `pom.xml`: Java/Maven dependencies and build setup.
- `mvnw`, `mvnw.cmd`: Maven wrapper.
- `HELP.md`: generated Spring help notes.
- `documentation/ledger-service/`: this service's documentation set. (if you're reading this, you're already here)
