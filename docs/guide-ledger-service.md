# Ledger service guide

**Purpose**: Provide a navigable overview of `ledger-service` documentation.
<br>
Last updated: 2026-04-22

## Overview

&nbsp;&nbsp;&nbsp;&nbsp; The ledger-service is the core of the system and is responsible for managing all financial data, including accounts, account types, journal entries, and <br>
transaction lines. It enforces the rules of double-entry accounting, ensuring that all transactions remain balanced and valid. The service is intentionally <br>
designed to remain focused on data integrity and does not include reporting or aggregation logic. Instead, it exposes structured APIs that provide raw <br>
financial data for other services to consume. This separation allows the ledger to remain simple, predictable, and highly reliable. By treating journal entries <br>
as the single source of truth, the service ensures long-term correctness and auditability.

## Responsibilities

- Manage chart of accounts (classifications, types, accounts)
- Manage journal entries and journal lines
- Enforce double-entry invariants and core data integrity

## Current capabilities

- CRUD-style APIs for account taxonomy and journal entries
- Sorting/filtering for search endpoints where implemented
- Consistent response envelope and centralized error handling

## Tech stack

- Java 17
- Spring Boot (Web MVC, Data JPA, Flyway)
- PostgreSQL
- Maven


## Docs

- [Architecture](./ledger-service/architecture.md): how it’s built and why it’s structured this way
- [Domain model](./ledger-service/domain-model.md): core business concepts and rules (“accounting brain”)
- [Data design](./ledger-service/data-design.md): how the domain maps to Postgres (schema/tables/constraints)
- [API reference](./ledger-service/api-reference.md): how to call the service (human-readable; code is canonical)
- [Error handling](./ledger-service/error-handling.md): error response shape and exception-to-HTTP mapping
- [Logging & observability](./ledger-service/logging-observability.md): how to debug issues in production
- [Project structure](./ledger-service/project-structure.md): where things live in the codebase
- [Running locally](./ledger-service/running-locally.md): local setup and startup
- [Testing](./ledger-service/testing.md): how to run and reason about tests
- [Use cases](./ledger-service/use-cases.md): key workflows and how parts interact

