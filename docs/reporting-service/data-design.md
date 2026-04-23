# Reporting service data design

**Purpose**: Describe how `reporting-service` stores report data in Postgres.
<br>
Last updated: 2026-04-22

Back to: [Reporting service guide](../guide-reporting-service.md)

## Database and schema

- **Database**: PostgreSQL (shared in local dev via `docker-compose.yml`)
- **Schema**: `reporting` (see `reporting-service/src/main/resources/application.yml`)
- **Migrations**: Flyway under [`../../reporting-service/src/main/resources/db/migration/`](../../reporting-service/src/main/resources/db/migration/)

## Purpose of the data

The `reporting` schema exists to support asynchronous report generation without impacting transactional workloads:

- A durable job queue (`report_jobs`) to track requested work and outcomes
- Durable storage (`completed_reports`) for finished PDF outputs

## Flyway usage

Flyway migrations ensure the reporting schema is reproducible and evolves safely as report types and job metadata change.

See ADRs:

- [`../ADR/004_Use-Flyway-for-schema-management.md`](../ADR/004_Use-Flyway-for-schema-management.md)
- [`../ADR/013_Reporting-handled-asynchronously-via-job-queue.md`](../ADR/013_Reporting-handled-asynchronously-via-job-queue.md)

## Migration history

Migrations live in [`../../reporting-service/src/main/resources/db/migration/`](../../reporting-service/src/main/resources/db/migration/).

## Tables

- `report_jobs`: report generation requests and lifecycle state
- `completed_reports`: PDF content stored as `BYTEA`, keyed by `report_job_id`

## Status model

`report_jobs.status` is constrained to:

- `QUEUED`
- `IN_PROGRESS`
- `COMPLETED`
- `FAILED`

