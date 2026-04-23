# User interface service API reference

**Purpose**: Describe the external APIs `user-interface-service` depends on and how it consumes them.
<br>
Last updated: 2026-04-22

Back to: [User interface service guide](../guide-user-interface-service.md)

## Backend APIs

The UI consumes:

- `ledger-service` (default local `http://localhost:8081`)
- `user-service` (default local `http://localhost:8082`)
- `reporting-service` (default local `http://localhost:8083`)

## Canonical source

This page links to the backend APIs the UI calls. For detailed service contracts, see:

- Ledger service guide: [`../guide-ledger-service.md`](../guide-ledger-service.md)
- User service guide: [`../guide-user-service.md`](../guide-user-service.md)
- Reporting service guide: [`../guide-reporting-service.md`](../guide-reporting-service.md)

## UI routes

The UI’s client-side routes are defined in `user-interface-service/src/app/app.routes.ts` and include:

- `/login`, `/register`
- `/ledger`, `/pending-journal-entries`, `/accounts`, `/account-types`, `/reports` (guarded)

