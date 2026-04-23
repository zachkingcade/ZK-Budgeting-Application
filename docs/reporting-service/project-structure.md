# Reporting service project structure

**Purpose**: Explain where code lives in `reporting-service` so contributors can navigate quickly.
<br>
Last updated: 2026-04-22

Back to: [Reporting service guide](../guide-reporting-service.md)

## High-level layout

Key entrypoints:

- Controllers: [`../../reporting-service/src/main/java/zachkingcade/dev/reporting/adapter/web/`](../../reporting-service/src/main/java/zachkingcade/dev/reporting/adapter/web/)
- Web DTOs: [`../../reporting-service/src/main/java/zachkingcade/dev/reporting/adapter/web/dto/`](../../reporting-service/src/main/java/zachkingcade/dev/reporting/adapter/web/dto/)
- Outbound adapters: [`../../reporting-service/src/main/java/zachkingcade/dev/reporting/adapter/outbound/`](../../reporting-service/src/main/java/zachkingcade/dev/reporting/adapter/outbound/)
- Persistence: [`../../reporting-service/src/main/java/zachkingcade/dev/reporting/adapter/persistence/`](../../reporting-service/src/main/java/zachkingcade/dev/reporting/adapter/persistence/)
- Flyway migrations: [`../../reporting-service/src/main/resources/db/migration/`](../../reporting-service/src/main/resources/db/migration/)
- Runtime config: [`../../reporting-service/src/main/resources/application.yml`](../../reporting-service/src/main/resources/application.yml)

