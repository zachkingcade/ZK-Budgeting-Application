# Reporting service guide

**Purpose**: Provide a navigable overview of `reporting-service` documentation.
<br>
Last updated: 2026-04-22

## Overview

&nbsp;&nbsp;&nbsp;&nbsp; The reporting-service is responsible for generating financial reports and handling all aggregation logic within the system. It retrieves raw data from the <br>
ledger-service and processes it to produce outputs such as account balances, summaries, and downloadable reports. Report generation is handled asynchronously <br>
through a job queue, allowing the system to manage long-running or resource-intensive operations without blocking user requests. The service stores generated <br>
reports and provides endpoints for tracking job status and retrieving completed results. It operates independently from the ledger, ensuring that reporting <br>
workloads do not impact transactional performance. This design allows reporting capabilities to evolve and scale without affecting core financial operations.

## Responsibilities

- Aggregate ledger data into reports
- Queue report jobs and track status
- Store and serve completed report outputs (PDF)

## Current capabilities

- `/reports/catalog` for report type discovery
- Asynchronous report requests via `/reports/requests`
- Job list/get and PDF download endpoints

## Tech stack

- Java 17
- Spring Boot (Web MVC, Data JPA, Flyway, OAuth2 resource server)
- PostgreSQL
- Maven

## Docs

- [Architecture](./reporting-service/architecture.md)
- [Domain model](./reporting-service/domain-model.md)
- [Data design](./reporting-service/data-design.md)
- [API reference](./reporting-service/api-reference.md)
- [Error handling](./reporting-service/error-handling.md)
- [Logging & observability](./reporting-service/logging-observability.md)
- [Project structure](./reporting-service/project-structure.md)
- [Running locally](./reporting-service/running-locally.md)
- [Testing](./reporting-service/testing.md)
- [Use cases](./reporting-service/use-cases.md)

