# Ledger Service Overview

## Purpose
The `ledger-service` is the accounting core of this project. It stores and validates chart-of-accounts data and journal entries using double-entry rules.

## Scope
- Manage account classifications (`creditEffect`, `debitEffect`).
- Manage account types linked to classifications.
- Manage accounts linked to account types.
- Create and update journal entries with journal lines.
- Enforce balancing and structural accounting rules in the domain layer.

## Current Capabilities
- HTTP API for account, account type, account classification, and journal entry workflows.
- Domain-driven validation using `DomainException` and `ApplicationException`.
- Persistence through Spring Data JPA adapters and PostgreSQL.
- Flyway baseline migration (`V1__Init.sql`) for schema creation.

## Tech Stack
- Java 17
- Spring Boot (Web MVC, Data JPA, Flyway)
- PostgreSQL
- Maven

## Bounded Context
This service models general ledger concerns:
- account taxonomy
- chart of accounts
- journal entry posting structure
