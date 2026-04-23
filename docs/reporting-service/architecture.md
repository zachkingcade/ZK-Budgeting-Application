# Reporting service architecture

**Purpose**: Describe how `reporting-service` is built and how it interacts with other services.
<br>
Last updated: 2026-04-22

Back to: [Reporting service guide](../guide-reporting-service.md)

## Dependencies

In local dev, this service is wired to:

- `ledger-service` via `LEDGER_SERVICE_BASE_URL` (default `http://localhost:8081`)
- `user-service` via `USER_SERVICE_BASE_URL` (default `http://localhost:8082`)

Default local port: `8083`.

## Clean Architecture in this service

- **Domain**: report job state and rules around ownership and availability.
- **Application**: orchestration that queues jobs, calls outbound APIs, stores completed outputs.
- **Adapters**: `/reports` controller and DTOs, persistence (JPA), outbound HTTP clients to ledger/user services, OAuth2 resource server configuration.

See: [`../standards/microservice-springboot-consistency.md`](../standards/microservice-springboot-consistency.md) and ADRs under `../ADR/` (notably async reporting/job queue decisions).

## Canonical sources

This page describes intent; canonical wiring and configuration live in:

- Compose wiring: [`../../docker-compose.yml`](../../docker-compose.yml)
- Service config: [`../../reporting-service/src/main/resources/application.yml`](../../reporting-service/src/main/resources/application.yml)
- Cross-service conventions: [`../standards/microservice-springboot-consistency.md`](../standards/microservice-springboot-consistency.md)

