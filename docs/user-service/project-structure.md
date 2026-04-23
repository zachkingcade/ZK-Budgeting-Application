# User service project structure

**Purpose**: Explain where code lives in `user-service` so contributors can navigate quickly.
<br>
Last updated: 2026-04-22

Back to: [User service guide](../guide-user-service.md)

## High-level layout

Key entrypoints:

- Controllers: [`../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/`](../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/)
- Web DTOs: [`../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/dto/`](../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/dto/)
- Persistence: [`../../user-service/src/main/java/zachkingcade/dev/user/adapter/persistence/`](../../user-service/src/main/java/zachkingcade/dev/user/adapter/persistence/)
- Flyway migrations: [`../../user-service/src/main/resources/db/migration/`](../../user-service/src/main/resources/db/migration/)
- Runtime config: [`../../user-service/src/main/resources/application.yml`](../../user-service/src/main/resources/application.yml)

