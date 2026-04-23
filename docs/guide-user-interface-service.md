# User interface service guide

**Purpose**: Provide a navigable overview of `user-interface-service` documentation.
<br>
Last updated: 2026-04-22

## Overview

`user-interface-service` is the Angular-based UI for the system. It calls `ledger-service`, `user-service`, and `reporting-service` APIs.

## Responsibilities

- Provide the user-facing UI for ledger and reporting workflows
- Orchestrate calls to backend APIs and present results

## Current capabilities

- Auth pages and guarded app routes
- Ledger/account/account-type views and pending transactions UI
- Reports UI backed by `reporting-service`

## Tech stack

- Angular + TypeScript
- Angular Material (and related UI libs)

## Docs

- [Architecture](./user-interface-service/architecture.md)
- [Domain model](./user-interface-service/domain-model.md)
- [Data design](./user-interface-service/data-design.md)
- [API reference](./user-interface-service/api-reference.md)
- [Error handling](./user-interface-service/error-handling.md)
- [Logging & observability](./user-interface-service/logging-observability.md)
- [Project structure](./user-interface-service/project-structure.md)
- [Running locally](./user-interface-service/running-locally.md)
- [Testing](./user-interface-service/testing.md)
- [Use cases](./user-interface-service/use-cases.md)

