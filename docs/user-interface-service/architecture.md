# User interface service architecture

**Purpose**: Describe how `user-interface-service` is structured and how it communicates with backend services.
<br>
Last updated: 2026-04-22

Back to: [User interface service guide](../guide-user-interface-service.md)

## Architecture

This UI follows a layered structure (presentation/application/domain/adapter) consistent with the project’s Angular standards.

## Clean Architecture in this service

The UI mirrors Clean Architecture concepts to keep the presentation layer thin:

- **Presentation**: components/pages and display state.
- **Application**: orchestration and coordination between UI and adapters.
- **Domain**: UI-side models and rules (keep minimal; backend services own business logic).
- **Adapter**: API clients and DTO mapping.

See: [`../standards/standards-angular.md`](../standards/standards-angular.md) and ADRs under `../ADR/`.

## Service communication

At runtime (local docker compose), the UI is served on `http://localhost:4200` and calls backend services on:

- `ledger-service`: `http://localhost:8081`
- `user-service`: `http://localhost:8082`
- `reporting-service`: `http://localhost:8083`

## Reference

- Angular standards: [`../standards-angular.md`](../standards/standards-angular.md)

