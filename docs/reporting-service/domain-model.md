# Reporting service domain model

**Purpose**: Define the core business concepts owned by `reporting-service`.
<br>
Last updated: 2026-04-22

Back to: [Reporting service guide](../guide-reporting-service.md)

## Domain concepts

- **Report job**: a request to generate a report for a user.
- **Completed report**: stored output for a completed report job (PDF).

## Concepts

- **ReportJob**: tracks who requested the report, requested parameters, and lifecycle status.
- **CompletedReport**: stores the PDF bytes for a completed job (keyed by `report_job_id`).

## Ownership and access

Report jobs are user-owned. API endpoints that list/get/download are scoped to the authenticated user id.

## Canonical source

This page describes intent; canonical definitions live in:

- Flyway migration: [`../../reporting-service/src/main/resources/db/migration/`](../../reporting-service/src/main/resources/db/migration/)
- Persistence entities: [`../../reporting-service/src/main/java/zachkingcade/dev/reporting/adapter/persistence/jpa/`](../../reporting-service/src/main/java/zachkingcade/dev/reporting/adapter/persistence/jpa/)

