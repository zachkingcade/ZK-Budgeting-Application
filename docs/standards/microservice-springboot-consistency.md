# Spring Boot microservice consistency

**Purpose**: Align Spring Boot microservices on structure, API envelopes, and shared conventions.
<br>
Last updated: 2026-04-22

This document was written shortly after `ledger-service` and `user-service` reached their current shapes. Its purpose is to align future Spring Boot microservices with the patterns, API contracts, and layering already in use—so new services do not drift accidentally from the pattern.

## Canonical package layout

For **new** microservices, use **flat adapter packages** and keep Spring `@Configuration` next to the web layer:

| Layer | Package | Contents |
|------|---------|----------|
| Web adapter | `adapter/web` | REST controllers, web DTOs (`adapter/web/dto/...`), `ApiResponse`, `MetaData`, `GlobalExceptionHandler`, `RequestTimingFilter`, `ApiResponseMetaAdvice` |
| Web config | `adapter/web/config` | `SecurityConfig`, CORS, JWT decoder (when using resource server), other web-facing beans |
| Persistence adapter | `adapter/persistence` | `jpa/*Entity`, `repository/*JpaRepository`, `*PersistenceAdapter` implementing outbound ports |
| Application | `application` | `*Service` implementations, `commands/`, `port/in/**`, `port/out/**`, optional `results/` |
| Domain | `domain` | Domain models, domain exceptions as needed |

## Core philosophy

- Code should be **easy to read first, efficient second**
- Prefer **explicit over implicit** and **simple over abstract**
- Avoid premature optimization and unnecessary complexity
- Do not mix responsibilities across layers

## Naming conventions

- Variables/members: `camelCase`
- Methods: `camelCase`, verb-first
- Classes/interfaces: `PascalCase`
- Java interfaces: **no `I` prefix** (TypeScript may use `I` prefixes; see [`./standards-angular.md`](standards-angular.md))

## Layered architecture

Use three top-level layers: `adapter`, `application`, `domain`.

### Adapter

- HTTP/persistence wiring and other infrastructure-facing code
- Maps HTTP requests → application commands/use cases
- Maps application results → response DTOs (never expose JPA entities)

### Application

- Implements use cases
- Orchestrates domain + repository ports
- Dependency inversion: depend on ports, not concrete adapters

### Domain

- Business rules, validation, invariants
- **Pure Java** (no Spring annotations)

## API design

### Success envelope

```json
{
  "statusMessage": "Returned [10] Accounts",
  "metaData": {
    "requestDate": "2026-03-31",
    "requestTime": "12:34:56.789",
    "executionTimeMs": 12,
    "dataResponseCount": 10
  },
  "data": {}
}
```

### Error envelope

```json
{
  "errorCode": "DOMAIN_ERROR",
  "message": "Human readable message"
}
```

## Build and runtime baseline

- Java 17
- Spring Boot 4.x
- Shared Postgres DB in local dev; schema per service (e.g. `ledger`, `auth`, `reporting`)
- Assign a unique `server.port` per service (ledger: 8081, user: 8082, reporting: 8083)

## Relationship to the Angular / UI standards

The front-end standards live at [`./standards-angular.md`](standards-angular.md). Backend microservices should stay compatible with the expected envelopes and avoid uncoordinated breaking changes to JSON shapes.

## When something is not covered here

Choose the option that is easiest to understand and maintain later, and update this document when a new pattern repeats across services.

