Purpose: Describe key user workflows supported by the UI and how screens fit together.
Last updated: 2026-04-22

## Use cases

- **Authentication**
  - `GET /login`: login page
  - `GET /register`: registration page
- **Ledger**
  - `GET /ledger`: ledger page (guarded)
  - `GET /pending-journal-entries`: pending journal entries (guarded)
  - `GET /accounts`: accounts page (guarded)
  - `GET /account-types`: account types page (guarded)
- **Reports**
  - `GET /reports`: reports page (guarded)

Routes are defined in `user-interface-service/src/app/app.routes.ts` (canonical).

