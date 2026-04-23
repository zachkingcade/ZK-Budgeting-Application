# User interface service data design

**Purpose**: Describe how `user-interface-service` handles data shapes and persistence (if any).
<br>
Last updated: 2026-04-22

Back to: [User interface service guide](../guide-user-interface-service.md)

## Data handling

- API calls live in the adapter layer.
- External DTOs should be mapped into UI models before broad use.

## Purpose of the data

The UI exists to present backend-owned business data. Its “data design” is primarily about safe usage of external shapes:

- Keep adapter DTOs close to API clients
- Map DTOs into UI models before broad use
- Avoid duplicating backend invariants; treat services as authoritative

## Canonical sources

- UI layering standards: [`../standards/standards-angular.md`](../standards/standards-angular.md)
- ADR: [`../ADR/009_Angular-frontend-keeps-logic-minimal.md`](../ADR/009_Angular-frontend-keeps-logic-minimal.md)

