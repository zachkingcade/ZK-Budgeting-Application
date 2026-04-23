# User interface service domain model

**Purpose**: Define the core UI concepts and models used by `user-interface-service`.
<br>
Last updated: 2026-04-22

Back to: [User interface service guide](../guide-user-interface-service.md)

## Domain model

The UI keeps local models that mirror backend DTOs where needed, but should avoid letting raw API shapes leak throughout the codebase.

## Concepts

- **UI models**: types used broadly across presentation/application layers.
- **Adapter DTOs**: types that represent external service responses and requests.

## Policies

- UI domain logic should remain minimal; backend services own business rules.
- Map adapter DTOs into UI models at the adapter/application boundary.

## Canonical source

This page describes intent; the canonical definitions live in code:

- Adapter DTOs: [`../../user-interface-service/src/app/adapter/`](../../user-interface-service/src/app/adapter/)
- UI domain models: [`../../user-interface-service/src/app/domain/`](../../user-interface-service/src/app/domain/)

