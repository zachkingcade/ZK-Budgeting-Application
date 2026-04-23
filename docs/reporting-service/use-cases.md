Purpose: Describe key workflows supported by `reporting-service`.
Last updated: 2026-04-22

## Use cases

- **Catalog**: return the list of report types and parameters the UI can request.
- **Queue report**: create a `report_jobs` row for a user with requested parameters.
- **Track status**: expose job state transitions (`QUEUED`, `IN_PROGRESS`, `COMPLETED`, `FAILED`) via list/get.
- **Download**: return stored PDF bytes for a completed job via `/reports/{id}/download`.

